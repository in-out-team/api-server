package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.word.ReadOrCreateWordApplication
import com.inout.apiserver.infrastructure.db.word.LanguageTypes
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/words")
class WordController(
    private val readOrCreateWordApplication: ReadOrCreateWordApplication
) {
    @GetMapping
    fun getWord(@RequestParam name: String, @RequestParam language: LanguageTypes): ResponseEntity<WordResponse> {
        return ResponseEntity.ok(readOrCreateWordApplication.run(name, language))
    }
}