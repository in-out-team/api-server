package com.inout.apiserver.error

class InternalServerErrorException(
    override val message: String,
    val code: String
) : RuntimeException(message)
