package com.csg.codeit.controller

import com.csg.codeit.model.EvaluationResultRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class StudentController {
    @PostMapping(value = ["/"], consumes = [MediaType.TEXT_PLAIN_VALUE])
    fun evaluate(@RequestBody txt: String): ResponseEntity<String> {
        println(txt)
        return ResponseEntity<String>("{\"instructions\":null}", null, HttpStatus.ACCEPTED)
    }
}