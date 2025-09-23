package com.example.demas_api.controller

import com.example.demas_api.dto.ApiResponse
import com.example.demas_api.dto.MedicineTotalStock
import com.example.demas_api.dto.PaginationResponse
import com.example.demas_api.model.enumeration.DrugClassification
import com.example.demas_api.model.enumeration.MedicineAvailability
import com.example.demas_api.service.HealthUnitService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/medicines")
@Tag(name = "Medicamentos", description = "Endpoints para obter dados agregados e resumos de medicamentos.")
class MedicinesController(
    private val healthUnitService: HealthUnitService
) {
    @GetMapping("/summary/all")
    @Operation(
        summary = "Agrupa e resume o estoque de todos os medicamentos",
        description = "Retorna uma lista paginada com o resumo do estoque total de cada medicamento, somando as quantidades de todas as unidades de saúde. Permite filtrar por medicamentos com estoque (`AVAILABLE`), sem estoque (`UNAVAILABLE`) ou todos (`ALL`)."
    )
    fun getAllMedicineSummaries(
        @Parameter(description = "Número da página a ser retornada (inicia em 0).")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Quantidade de itens por página.")
        @RequestParam(defaultValue = "10") size: Int,

        @Parameter(description = "Termo de busca para filtrar os medicamentos pelo nome.")
        @RequestParam(required = false, defaultValue = "") searchTerm: String,

        @Parameter(description = "Filtro para exibir populares (`BASIC`), de alto custo (`EXPENSIVE`), ou todos (`ALL`)")
        @RequestParam(defaultValue = "BASIC") classification: DrugClassification,

        @Parameter(description = "Filtro para exibir medicamentos com estoque (`AVAILABLE`), sem estoque (`UNAVAILABLE`), ou todos (`ALL`).")
        @RequestParam(defaultValue = "ALL") filter: MedicineAvailability
    ): ResponseEntity<ApiResponse<MedicineTotalStock>> {
        val summaries = healthUnitService.getAggregatedMedicineStock(page, size, searchTerm, filter, classification)
        return ResponseEntity.ok(
            ApiResponse(
                data = summaries.content,
                pagination = PaginationResponse(
                    page = summaries.number,
                    size = summaries.size,
                    totalElements = summaries.totalElements,
                    totalPages = summaries.totalPages
                )
            )
        )
    }
}