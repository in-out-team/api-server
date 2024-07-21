package com.inout.apiserver.error

class InvalidCredentialsException(
    override val message: String,
    override val code: String,
    override val extraData: Map<String, Any?> = emptyMap()
) : RuntimeException(message), HttpException
