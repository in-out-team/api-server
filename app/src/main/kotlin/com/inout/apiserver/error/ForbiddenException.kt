package com.inout.apiserver.error

class ForbiddenException(
    override val message: String,
    val code: String,
) : RuntimeException(message)
