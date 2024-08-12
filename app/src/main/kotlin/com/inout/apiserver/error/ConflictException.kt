package com.inout.apiserver.error

class ConflictException(
    override val message: String,
    val code: String
) : RuntimeException(message)
