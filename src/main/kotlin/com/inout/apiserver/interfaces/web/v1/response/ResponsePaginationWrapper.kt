package com.inout.apiserver.interfaces.web.v1.response

data class ResponsePaginationWrapper<T>(
    val data: List<T>,
    val total: Int,
    val page: Int,
    val size: Int
)
