package com.inout.apiserver.error

class InOutRequireNotNullException(
    override val message: String,
    val code: String
) : RuntimeException(message)
