package com.inout.apiserver.config

import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.InOutRequireNotNullException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

data class ErrorResponse(
    val code: String,
    val message: String
)

@ControllerAdvice
class ControllerAdvice {
    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(e: ConflictException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(code = e.code, message = e.message ?: "Conflict"))
    }

    /**
     * InOutRequireNotNullException
     * - when domain is expected to have a value but is null
     * Codes:
     * - IORNN_USER_1: User id is null from CreateUserApplication
     */
    @ExceptionHandler(InOutRequireNotNullException::class)
    fun handleInOutRequireNotNullException(e: InOutRequireNotNullException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(code = e.code, message = e.message ?: "Internal Server Error"))
    }
}