package com.example.demas_api.controller

import com.example.demas_api.dto.MessageResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
@Tag(name = "Health Controller", description = "Endpoint para Health Check da API")
class ApiController {
    @GetMapping
    fun health() = ResponseEntity.status(HttpStatus.OK).body(MessageResponse("Healthy"))
}