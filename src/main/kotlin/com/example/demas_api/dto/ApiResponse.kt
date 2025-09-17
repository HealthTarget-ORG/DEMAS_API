package com.example.demas_api.dto

class ApiResponse<T>(
    val data: MutableList<T?>?,
    val pagination: PaginationResponse?
)