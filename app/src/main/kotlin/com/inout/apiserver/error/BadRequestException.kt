package com.inout.apiserver.error

class BadRequestException(
    override val message: String,
    val code: String
) : RuntimeException(message)
