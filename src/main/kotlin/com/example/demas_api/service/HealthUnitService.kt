package com.example.demas_api.service

import com.example.demas_api.dto.MedicineTotalStock
import com.example.demas_api.model.HealthUnit
import com.example.demas_api.model.MedicineStock
import com.example.demas_api.model.enumeration.DrugClassification
import com.example.demas_api.model.enumeration.LocationType
import com.example.demas_api.model.enumeration.MedicineAvailability
import com.example.demas_api.repository.HealthUnitRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class HealthUnitService(
    val healthUnitRepository: HealthUnitRepository
) {
    fun getAll(
        page: Int,
        size: Int,
        searchTerm: String,
        filter: LocationType
    ): Page<HealthUnit> {
        val pageable = PageRequest.of(page, size)
        val search = if (searchTerm.isBlank()) ".*" else Pattern.quote(searchTerm)

        return if (filter == LocationType.ALL) {
            healthUnitRepository.findBySearchTerm(search, pageable)
        } else {
            healthUnitRepository.findBySearchTermAndLocationType(search, filter, pageable)
        }
    }

    fun findMedicinesByUnit(cnesCode: String, page: Int, size: Int): Page<MedicineStock> {
        val pageable = PageRequest.of(page, size)

        if (!healthUnitRepository.existsById(cnesCode)) {
            return Page.empty(pageable)
        }

        val content = healthUnitRepository.findAndPaginateMedicinesByUnit(cnesCode, pageable)
        val totalElements = healthUnitRepository.countMedicinesByUnit(cnesCode)

        return PageImpl(content, pageable, totalElements)
    }

    fun getAggregatedMedicineStock(
        page: Int,
        size: Int,
        searchTerm: String,
        filter: MedicineAvailability,
        classification: DrugClassification
    ): Page<MedicineTotalStock> {
        val pageable = PageRequest.of(page, size)
        val search = searchTerm.ifBlank { ".*" }

        val classificationRegex = when (classification) {
            DrugClassification.EXPENSIVE -> "1\\.A|1\\.B"
            DrugClassification.BASIC -> "^((?!1\\.A|1\\.B).)*\$"
            DrugClassification.ALL -> ".*"
        }

        val content: List<MedicineTotalStock>
        val totalElements: Long

        when (filter) {
            MedicineAvailability.AVAILABLE -> {
                content = healthUnitRepository.aggregateStockAvailable(search, classificationRegex, pageable)
                totalElements = healthUnitRepository.countStockAvailable(search, classificationRegex) ?: 0L
            }

            MedicineAvailability.UNAVAILABLE -> {
                content = healthUnitRepository.aggregateStockUnavailable(search, classificationRegex, pageable)
                totalElements = healthUnitRepository.countStockUnavailable(search, classificationRegex) ?: 0L
            }

            MedicineAvailability.ALL -> {
                content = healthUnitRepository.aggregateStockAll(search, classificationRegex, pageable)
                totalElements = healthUnitRepository.countStockAll(search, classificationRegex) ?: 0L
            }
        }

        return PageImpl(content, pageable, totalElements)
    }

    fun findUnitsByMedicine(
        page: Int,
        size: Int,
        searchTerm: String
    ): Page<HealthUnit> {
        val pageable = PageRequest.of(page, size)
        val search = if (searchTerm.isBlank()) ".*" else Pattern.quote(searchTerm)

        val content = healthUnitRepository.findUnitsWithMedicineInStock(search, pageable)
        val totalElements = healthUnitRepository.countUnitsWithMedicineInStock(search)

        return PageImpl(content, pageable, totalElements)
    }

}