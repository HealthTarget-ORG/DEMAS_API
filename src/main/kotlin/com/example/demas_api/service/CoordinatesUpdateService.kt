package com.example.demas_api.service

import com.example.demas_api.model.Address
import com.example.demas_api.model.HealthUnit
import com.example.demas_api.model.enumeration.LocationType
import com.example.demas_api.repository.HealthUnitRepository
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class CoordinatesUpdateService(
    private val healthUnitRepository: HealthUnitRepository,
    private val geoApiContext: GeoApiContext
) {
    private val logger = LoggerFactory.getLogger(CoordinatesUpdateService::class.java)

    @Scheduled(cron = "0 20 4 * * *", zone = "America/Sao_Paulo")
    fun verifyAndCompleteUrbanUnitCoordinates() {
        logger.info("Iniciando verificação agendada de coordenadas de unidades urbanas.")

        var updatedCount = 0
        var pageNumber = 0
        val pageSize = 100
        var unitPage: Page<HealthUnit>

        do {
            val pageable: Pageable = PageRequest.of(pageNumber, pageSize)
            logger.info("Processando página {} de unidades urbanas...", pageNumber)


            unitPage = healthUnitRepository.findBySearchTermAndLocationType(
                searchTerm = "",
                locationType = LocationType.URBANA,
                pageable = pageable
            )

            val urbanUnitsOnPage = unitPage.content
            logger.debug("Encontradas {} unidades urbanas na página {}.", urbanUnitsOnPage.size, pageNumber)

            for (unit in urbanUnitsOnPage) {
                val longitude = unit.location.x
                val latitude = unit.location.y

                if (isCoordinateIncomplete(longitude) || isCoordinateIncomplete(latitude)) {
                    logger.warn(
                        "Coordenadas incompletas para a unidade {}: [{}, {}]. Tentando geocodificar.",
                        unit.cnesCode,
                        latitude,
                        longitude
                    )

                    try {
                        val newCoordinates = geocodeAddress(unit.address, unit.city, unit.state)
                        if (newCoordinates != null) {
                            unit.location = newCoordinates
                            healthUnitRepository.save(unit)
                            updatedCount++
                            logger.info(
                                "Coordenadas da unidade {} atualizadas para: [{}, {}]",
                                unit.cnesCode,
                                newCoordinates.y,
                                newCoordinates.x
                            )
                        } else {
                            logger.error("Falha ao geocodificar o endereço da unidade {}.", unit.cnesCode)
                        }


                        TimeUnit.MILLISECONDS.sleep(50)

                    } catch (e: Exception) {
                        logger.error("Erro ao processar a unidade {}: {}", unit.cnesCode, e.message, e)
                    }
                }
            }

            pageNumber++
        } while (unitPage.hasNext())

        logger.info(
            "Verificação de coordenadas concluída. Total de unidades processadas: {}. Total atualizado: {}",
            unitPage.totalElements,
            updatedCount
        )
    }

    private fun isCoordinateIncomplete(coordinate: Double): Boolean {
        if (coordinate == 0.0) return true
        val coordinateStr = coordinate.toString()
        val decimalPointIndex = coordinateStr.indexOf('.')
        return if (decimalPointIndex != -1) {
            val decimalPlaces = coordinateStr.length - decimalPointIndex - 1
            decimalPlaces <= 3
        } else {
            true
        }
    }

    private fun geocodeAddress(address: Address, city: String?, state: String?): GeoJsonPoint? {
        val fullAddress = buildString {
            append(address.street ?: "")
            append(", ${address.number ?: "S/N"}")
            append(", ${address.neighborhood ?: ""}")
            append(" - ${city ?: ""}")
            append(", ${state ?: ""}")
            append(", ${address.cep ?: ""}")
        }.replace(" ,", ",").trim()

        if (fullAddress.length < 10) {
            logger.warn("Endereço muito curto para geocodificação: '{}'", fullAddress)
            return null
        }

        val results = GeocodingApi.geocode(geoApiContext, fullAddress).await()
        if (results.isNotEmpty()) {
            val location = results[0].geometry.location
            return GeoJsonPoint(location.lng, location.lat)
        }
        return null
    }
}