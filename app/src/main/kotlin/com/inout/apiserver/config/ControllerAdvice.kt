package com.inout.apiserver.config

import com.inout.apiserver.error.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

data class ErrorResponse(
    override val code: String,
    override val message: String,
    override val extraData: Map<String, Any?> = emptyMap()
) : HttpException

@ControllerAdvice
class ControllerAdvice {
    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(e: HttpException): ResponseEntity<ErrorResponse> {
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
    fun handleInOutRequireNotNullException(e: HttpException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(code = e.code, message = e.message ?: "Internal Server Error"))
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: HttpException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code = e.code, message = e.message ?: "Not Found"))
    }

    @ExceptionHandler(InvalidCredentialsException::class, GoogleIdTokenVerificationException::class)
    fun handleInvalidCredentialsException(e: HttpException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(code = e.code, message = e.message ?: "Unauthorized"))
    }

    @ExceptionHandler(InternalServerErrorException::class)
    fun handleInternalServerErrorException(e: HttpException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(code = e.code, message = e.message ?: "Internal Server Error"))
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(e: HttpException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = e.code, message = e.message ?: "Bad Request"))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = "BAD_REQUEST_1", message = e.message ?: "Bad Request"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorMap = e.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = "BAD_REQUEST_2", message = "Bad Request", extraData = errorMap))
    }
}