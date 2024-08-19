package com.inout.apiserver.error

interface UnauthorizedException : HttpException {
    override val message: String
    override val code: String
    override val extraData: Map<String, Any?>
}
