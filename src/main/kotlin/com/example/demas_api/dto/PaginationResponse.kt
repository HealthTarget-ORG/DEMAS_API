package com.example.demas_api.dto


data class PaginationResponse(
    val page: Int?,
    val size: Int?,
    val totalElements: Long?,
    val totalPages: Int?
)