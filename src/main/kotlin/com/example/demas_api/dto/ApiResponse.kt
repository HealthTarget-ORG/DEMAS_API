package com.example.demas_api.dto

class ApiResponse<T>(
    val data: List<T?>,
    val pagination: PaginationResponse?
)