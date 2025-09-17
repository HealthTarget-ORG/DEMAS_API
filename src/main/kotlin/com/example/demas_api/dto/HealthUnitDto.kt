package com.example.demas_api.dto

import com.example.demas_api.model.Address
import com.example.demas_api.model.enumeration.LocationType
import java.time.LocalDate

data class HealthUnitDto(
    val cnesCode: String,
    val name: String,
    val address: Address,
    val long: Double,
    val lat: Double,
    val phone: String?,
    val email: String?,
    val locationType: LocationType,
    val city: String,
    val state: String,
    val lastStockUpdate: LocalDate
)
