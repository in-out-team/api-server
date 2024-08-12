package com.inout.apiserver.error

interface HttpException {
    val message: String
    val code: String
    val extraData: Map<String, Any?>
}
