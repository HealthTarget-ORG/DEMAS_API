package com.example.demas_api.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DemasApiResponseDto(
    val parametros: List<DemasDataItemDto>
)
