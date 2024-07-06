package com.inout.apiserver.error

interface HttpException {
    val message: String
    val code: String
}
