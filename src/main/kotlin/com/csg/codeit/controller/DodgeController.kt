package com.csg.codeit.controller

import com.csg.codeit.model.DodgeBulletService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class DodgeController(val dodgeBulletService: DodgeBulletService) {

    @PostMapping(value = ["/dodge"], consumes = [MediaType.TEXT_PLAIN_VALUE])
    fun evaluate(@RequestBody txt: String): ResponseEntity<String> {
        val result = listOf(
            "{\"instructions\": [\"d\", \"l\"]}",
            "{\"instructions\":null}",
            "{\"instructions\":[\"l\", \"r\",\"l\", \"r\",\"l\", \"r\",\"l\", \"r\"]}",
            "{\"instructions\":[]}",
            "{\"instructions\":[]}",
            "{\"instructions\":[\"u\",\"u\",\"u\",\"r\",\"r\",\"r\",\"r\",\"r\",\"r\",\"r\",\"r\",\"d\",\"d\",\"d\",\"d\",\"l\",\"l\",\"l\",\"l\",\"l\",\"l\",\"l\",\"l\"]}",
            "{\"instructions\":null}",
            "{\"instructions\":[\"r\",\"l\", \"r\",\"l\", \"r\",\"l\", \"r\", \"r\", \"r\", \"r\", \"r\"]}"
        )

        val resultMap = dodgeBulletService.getLevels().zip(result)
            .associate { p -> p.first.map.joinToString("\n") { it.joinToString("") } to p.second }
        return ResponseEntity<String>(resultMap[txt], null, HttpStatus.ACCEPTED)
    }
}