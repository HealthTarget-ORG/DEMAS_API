package com.example.demas_api.model

import org.springframework.data.mongodb.core.mapping.Field

data class MedicineStock(
    @Field("catmat_code")
    val catmatCode: String,

    @Field("description")
    val description: String,

    @Field("total_quantity")
    val totalQuantity: Int
)