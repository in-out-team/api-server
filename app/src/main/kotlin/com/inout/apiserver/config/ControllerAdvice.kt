package com.inout.apiserver.config

import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.ForbiddenException
import com.inout.apiserver.error.GoogleIdTokenVerificationException
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.error.InvalidCredentialsException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.error.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

data class ErrorResponse(
    override val code: String,
    override val message: String,
    override val extraData: Map<String, Any?> = emptyMap(),
) : HttpException

@ControllerAdvice
class ControllerAdvice {
    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(e: ConflictException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(code = e.code, message = e.message))
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
            .body(ErrorResponse(code = e.code, message = e.message))
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code = e.code, message = e.message))
    }

    @ExceptionHandler(InvalidCredentialsException::class, GoogleIdTokenVerificationException::class)
    fun handleInvalidCredentialsException(e: UnauthorizedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(code = e.code, message = e.message))
    }

    @ExceptionHandler(InternalServerErrorException::class)
    fun handleInternalServerErrorException(e: InternalServerErrorException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(code = e.code, message = e.message))
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(e: BadRequestException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = e.code, message = e.message))
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

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = "BAD_REQUEST_3", message = "Bad Request", extraData = mapOf("field" to e.name)))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(code = e.code, message = e.message))
    }
}
