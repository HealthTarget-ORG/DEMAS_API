package com.example.demas_api.mapper

import com.example.demas_api.dto.HealthUnitDto
import com.example.demas_api.model.HealthUnit

fun HealthUnit.toDto(): HealthUnitDto {
    return HealthUnitDto(
        cnesCode = this.cnesCode,
        name = this.name,
        address = this.address,
        long = this.location.x,
        lat = this.location.y,
        phone = this.phone,
        email = this.email,
        locationType = this.locationType,
        city = this.city,
        state = this.state,
        lastStockUpdate = this.lastStockUpdate
    )
}