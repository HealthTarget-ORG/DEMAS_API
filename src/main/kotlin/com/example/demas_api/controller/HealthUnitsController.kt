package com.example.demas_api.controller

import com.example.demas_api.dto.ApiResponse
import com.example.demas_api.dto.HealthUnitDto
import com.example.demas_api.dto.PaginationResponse
import com.example.demas_api.mapper.toDto
import com.example.demas_api.model.HealthUnit
import com.example.demas_api.model.MedicineStock
import com.example.demas_api.model.enumeration.LocationType
import com.example.demas_api.service.HealthUnitService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health-units")
@Tag(name = "Unidades de Saúde", description = "Endpoints para consultar informações sobre as unidades de saúde.")
class HealthUnitsController(
    private val healthUnitService: HealthUnitService
) {
    @GetMapping
    @Operation(
        summary = "Busca paginada de unidades de saúde",
        description = "Retorna uma lista paginada de todas as unidades de saúde. Permite uma busca opcional por nome da unidade."
    )
    fun getAll(
        @Parameter(description = "Número da página a ser retornada (inicia em 0).")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Quantidade de itens por página.")
        @RequestParam(defaultValue = "10") size: Int,

        @Parameter(description = "Termo de busca para filtrar unidades pelo nome.")
        @RequestParam(required = false, defaultValue = "") searchTerm: String,

        @Parameter(description = "Filtro por tipo de localidade da unidade. Pode ser (`DISTRITAL`), (`RURAL`), (`URBANA`) ou (`ALL`).")
        @RequestParam(defaultValue = "ALL") filter: LocationType,
    ): ResponseEntity<ApiResponse<HealthUnitDto>> {
        val healthUnitPage = healthUnitService.getAll(page, size, searchTerm, filter)
        val healthUnitDtoList = healthUnitPage.content.map { it.toDto() }

        val apiResponse = ApiResponse(
            data = healthUnitDtoList,
            pagination = PaginationResponse(
                page = healthUnitPage.number,
                size = healthUnitPage.size,
                totalElements = healthUnitPage.totalElements,
                totalPages = healthUnitPage.totalPages
            )
        )
        return ResponseEntity.ok(apiResponse)
    }

    @GetMapping("/by-medicine")
    @Operation(
        summary = "Encontra unidades que possuem um medicamento específico",
        description = "Busca por um nome de medicamento e retorna uma lista paginada das unidades de saúde que o possuem em estoque (> 0). A resposta, para cada unidade, mostra apenas o medicamento pesquisado e sua quantidade."
    )
    fun getUnitsByMedicine(
        @Parameter(description = "Número da página a ser retornada (inicia em 0).")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Quantidade de itens por página.")
        @RequestParam(defaultValue = "10") size: Int,

        @Parameter(description = "Nome (ou parte do nome) do medicamento a ser pesquisado.", required = true)
        @RequestParam searchTerm: String
    ): ResponseEntity<ApiResponse<HealthUnit>> {
        val unitsPage = healthUnitService.findUnitsByMedicine(page, size, searchTerm)
        return ResponseEntity.ok(
            ApiResponse(
                data = unitsPage.content,
                pagination = PaginationResponse(
                    page = unitsPage.number,
                    size = unitsPage.size,
                    totalElements = unitsPage.totalElements,
                    totalPages = unitsPage.totalPages
                )
            )
        )
    }

    @GetMapping("/{cnesCode}/medicines")
    @Operation(
        summary = "Lista os medicamentos de uma unidade específica de forma paginada",
        description = "Dado o código CNES de uma unidade, retorna uma lista paginada de medicamentos e seus estoques."
    )
    fun getMedicinesByUnit(
        @Parameter(description = "Código CNES da unidade de saúde.", required = true, example = "9707204")
        @PathVariable cnesCode: String,

        @Parameter(description = "Número da página a ser retornada (inicia em 0).")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Quantidade de itens por página.")
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ApiResponse<MedicineStock>> {
        val medicinesPage = healthUnitService.findMedicinesByUnit(cnesCode, page, size)

        if (medicinesPage.isEmpty && page > 0) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(
            ApiResponse(
                data = medicinesPage.content,
                pagination = PaginationResponse(
                    page = medicinesPage.number,
                    size = medicinesPage.size,
                    totalElements = medicinesPage.totalElements,
                    totalPages = medicinesPage.totalPages
                )
            )
        )
    }
}