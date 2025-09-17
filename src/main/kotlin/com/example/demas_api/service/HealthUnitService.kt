package com.example.demas_api.service

import com.example.demas_api.dto.MedicineTotalStock
import com.example.demas_api.model.HealthUnit
import com.example.demas_api.model.enumeration.MedicineAvailability
import com.example.demas_api.repository.HealthUnitRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class HealthUnitService(
    val healthUnitRepository: HealthUnitRepository
) {
    fun getAll(
        page: Int,
        size: Int,
        searchTerm: String,
    ): Page<HealthUnit> {
        val pageable = PageRequest.of(page, size)
        val search = searchTerm.ifBlank { ".*" }
        return  healthUnitRepository.findBySearchTerm(search, pageable)
    }

    fun getAggregatedMedicineStock(
        page: Int,
        size: Int,
        searchTerm: String,
        filter: MedicineAvailability
    ): Page<MedicineTotalStock> {
        val pageable = PageRequest.of(page, size)
        val search = searchTerm.ifBlank { ".*" }

        val content: List<MedicineTotalStock>
        val totalElements: Long

        when (filter) {
            MedicineAvailability.AVAILABLE -> {
                content = healthUnitRepository.aggregateStockAvailable(search, pageable)
                totalElements = healthUnitRepository.countStockAvailable(search)
            }
            MedicineAvailability.UNAVAILABLE -> {
                content = healthUnitRepository.aggregateStockUnavailable(search, pageable)
                totalElements = healthUnitRepository.countStockUnavailable(search)
            }
            MedicineAvailability.ALL -> {
                content = healthUnitRepository.aggregateStockAll(search, pageable)
                totalElements = healthUnitRepository.countStockAll(search)
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
        val search = searchTerm.ifBlank { ".*" }

        val content = healthUnitRepository.findUnitsWithMedicineInStock(search, pageable)
        val totalElements = healthUnitRepository.countUnitsWithMedicineInStock(search)

        return PageImpl(content, pageable, totalElements)
    }

}