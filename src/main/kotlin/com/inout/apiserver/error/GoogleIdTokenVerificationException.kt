package com.inout.apiserver.error

class GoogleIdTokenVerificationException(
    override val message: String,
    val code: String
) : RuntimeException(message)
