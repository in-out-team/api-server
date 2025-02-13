package com.inout.apiserver.interfaces.web.v1.response

data class ResponseListWrapper<T>(
    val data: List<T>,
)
