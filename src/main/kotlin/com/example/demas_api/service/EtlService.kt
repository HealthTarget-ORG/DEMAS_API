package com.example.demas_api.service

import com.example.demas_api.dto.DemasApiResponseDto
import com.example.demas_api.dto.DemasDataItemDto
import com.example.demas_api.model.Address
import com.example.demas_api.model.HealthUnit
import com.example.demas_api.model.MedicineStock
import com.example.demas_api.model.enumeration.LocationType
import com.example.demas_api.repository.HealthUnitRepository
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class EtlService(
    private val healthUnitRepository: HealthUnitRepository,
    webClientBuilder: WebClient.Builder
) {
    private val logger = LoggerFactory.getLogger(EtlService::class.java)
    private val demasApiWebClient: WebClient = webClientBuilder.baseUrl("https://apidadosabertos.saude.gov.br").build()

    @Scheduled(cron = "0 0 4 * * *", zone = "America/Sao_Paulo")
    fun runEtlProcess() {
        logger.info("Iniciando processo de ETL de estoque de medicamentos...")

        try {
            val latestApiDate = findLatestAvailableDataDate()
            if (latestApiDate == null) {
                logger.warn("Nenhuma data com dados encontrada na API nos últimos 30 dias. Processo encerrado.")
                return
            }
            logger.info("Data mais recente disponível na API DEMAS: {}", latestApiDate)

            val lastDbUpdateDate = healthUnitRepository.findTopByOrderByLastStockUpdateDesc()?.lastStockUpdate
            logger.info("Última data de atualização no banco de dados local: {}", lastDbUpdateDate)

            if (lastDbUpdateDate != null && !latestApiDate.isAfter(lastDbUpdateDate)) {
                logger.info("Os dados no banco de dados já estão atualizados. Nenhuma ação necessária.")
                return
            }

            logger.info("Novos dados encontrados na API. Iniciando extração completa...")

            val allRawData = fetchAllDemasData(latestApiDate)
            if (allRawData.isEmpty()) {
                logger.warn("Extração falhou ou não retornou dados para a data {}. Processo encerrado.", latestApiDate)
                return
            }
            logger.info("Extração concluída: {} registros brutos encontrados.", allRawData.size)

            val aggregatedHealthUnits = transformAndAggregate(allRawData)
            logger.info("Transformação concluída: {} unidades de saúde agregadas.", aggregatedHealthUnits.size)

            loadIntoDatabase(aggregatedHealthUnits)

        } catch (e: Exception) {
            logger.error("Erro crítico durante o processo de ETL: {}", e.message, e)
        }
    }

    private fun findLatestAvailableDataDate(): LocalDate? {
        var currentDate = LocalDate.now()
        val searchLimitInDays = 30

        for (i in 0 until searchLimitInDays) {
            val formattedDate = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            logger.info("Verificando se há dados para a data: $formattedDate...")

            try {
                val response = demasApiWebClient.get()
                    .uri { uriBuilder ->
                        uriBuilder.path("/daf/estoque-medicamentos-bnafar-horus")
                            .queryParam("codigo_uf", "23")
                            .queryParam("codigo_municipio", "230240")
                            .queryParam("data_posicao_estoque", formattedDate)
                            .queryParam("limit", 1).build()
                    }
                    .retrieve()
                    .bodyToMono<DemasApiResponseDto>()
                    .block()

                if (response?.parametros?.isNotEmpty() == true) {
                    return currentDate
                }
            } catch (e: Exception) {
                logger.error("Erro ao verificar a data $formattedDate. Tentando dia anterior. Erro: ${e.message}")
            }

            currentDate = currentDate.minusDays(1)
        }

        return null
    }

    private fun fetchAllDemasData(targetDate: LocalDate): List<DemasDataItemDto> {
        val allItems = mutableListOf<DemasDataItemDto>()
        var offset = 0
        val limit = 50
        val formattedDate = targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        logger.info("Iniciando extração completa para a data: $formattedDate")

        while (true) {
            logger.debug("Buscando lote de dados... offset: $offset")
            val response = demasApiWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/daf/estoque-medicamentos-bnafar-horus")
                        .queryParam("codigo_uf", "23")
                        .queryParam("codigo_municipio", "230240")
                        .queryParam("data_posicao_estoque", formattedDate)
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build()
                }
                .retrieve()
                .bodyToMono<DemasApiResponseDto>()
                .block()

            val items = response?.parametros ?: emptyList()

            if (items.isEmpty()) {
                logger.info("Nenhum item adicional encontrado. Fim da paginação.")
                break
            }

            allItems.addAll(items)
            offset += limit
        }

        return allItems
    }

    private fun transformAndAggregate(rawData: List<DemasDataItemDto>): List<HealthUnit> {
        val healthUnitsMap = mutableMapOf<String, MutableMap<String, MedicineStock>>()
        val healthUnitDetailsMap = mutableMapOf<String, DemasDataItemDto>()
        rawData.forEach { item ->
            healthUnitDetailsMap.putIfAbsent(item.codigoCnes, item)

            val medicinesForUnit = healthUnitsMap.getOrPut(item.codigoCnes) { mutableMapOf() }

            val existingMedicine = medicinesForUnit[item.codigoCatmat]

            if (existingMedicine != null) {
                val updatedMedicine = existingMedicine.copy(
                    totalQuantity = existingMedicine.totalQuantity + item.quantidadeEstoque
                )
                medicinesForUnit[item.codigoCatmat] = updatedMedicine
            } else {
                medicinesForUnit[item.codigoCatmat] = MedicineStock(
                    catmatCode = item.codigoCatmat,
                    description = item.descricaoProduto,
                    totalQuantity = item.quantidadeEstoque
                )
            }
        }

        return healthUnitDetailsMap.map { (cnes, details) ->
            val locationType = classifyLocation(details)

            val medicines = healthUnitsMap[cnes]?.values?.toList() ?: emptyList()
            HealthUnit(
                cnesCode = cnes,
                name = details.nomeFantasia,
                address = Address(
                    street = details.logradouro,
                    number = details.numeroEndereco,
                    neighborhood = details.bairro,
                    cep = details.cep
                ),
                location = GeoJsonPoint(
                    details.longitude ?: 0.0, details.latitude ?: 0.0
                ),
                phone = details.telefone,
                email = details.email,
                locationType = locationType,
                city = details.municipio,
                state = details.uf,
                lastStockUpdate = LocalDate.parse(details.dataPosicaoEstoque, DateTimeFormatter.ISO_LOCAL_DATE),
                medicines = medicines
            )
        }
    }

    private fun classifyLocation(unitDetails: DemasDataItemDto): LocationType {
        val street = unitDetails.logradouro?.uppercase() ?: ""
        val neighborhood = unitDetails.bairro?.uppercase() ?: ""

        return when {
            street.contains("DISTRITO") || neighborhood.contains("DISTRITO") -> LocationType.DISTRITAL
            neighborhood.contains("ZONA RURAL") -> LocationType.RURAL
            else -> LocationType.URBANA
        }
    }

    private fun loadIntoDatabase(healthUnits: List<HealthUnit>) {
        if (healthUnits.isNotEmpty()) {
            logger.info("Salvando ${healthUnits.size} unidades de saúde no banco de dados...")
            healthUnitRepository.saveAll(healthUnits)
            logger.info("Dados salvos com sucesso.")
        } else {
            logger.warn("Nenhuma unidade de saúde para carregar no banco de dados.")
        }
    }
}