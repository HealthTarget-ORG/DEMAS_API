package com.example.demas_api.model

import com.example.demas_api.model.enumeration.LocationType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDate

@Document(collection = "health_units")
data class HealthUnit(
    @Id val cnesCode: String,

    @Field("name")
    val name: String,

    @Field("address")
    val address: Address,

    @Field("location")
    var location: GeoJsonPoint,

    @Field("city")
    val city: String,

    @Field("state")
    val state: String,

    @Field("phone")
    val phone: String?,

    @Field("email")
    val email: String?,

    @Field("location_type")
    val locationType: LocationType,

    @Field("last_stock_update")
    val lastStockUpdate: LocalDate,

    @Field("medicines")
    val medicines: List<MedicineStock> = listOf()
)