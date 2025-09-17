package com.example.demas_api.controller

import com.example.demas_api.dto.MessageResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class ApiController {
    @GetMapping
    fun health() = ResponseEntity.status(HttpStatus.OK).body(MessageResponse("Healthy"))
}