package com.inout.apiserver.error

class NotFoundException(
    override val message: String,
    val code: String
) : RuntimeException(message)
