package com.inout.apiserver.error

class InvalidCredentialsException(
    override val message: String,
    val code: String
) : RuntimeException(message)
