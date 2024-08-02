package com.csg.codeit.controller

import com.csg.codeit.model.EvaluationResultRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

@RestController
class StudentController {
    @GetMapping(value = ["/"])
    fun home() = RedirectView("index.html")
    @PostMapping(value = ["/"], consumes = [MediaType.TEXT_PLAIN_VALUE])
    fun evaluate(@RequestBody txt: String): ResponseEntity<String> {
        return ResponseEntity<String>("{\"instructions\":null}", null, HttpStatus.ACCEPTED)
    }
}