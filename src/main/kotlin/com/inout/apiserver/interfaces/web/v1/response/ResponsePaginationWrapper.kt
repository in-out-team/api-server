package com.inout.apiserver.interfaces.web.v1.response

data class ResponsePaginationWrapper<T>(
    val data: List<T>,
    val hasMore: Boolean,
    val count: Long,
)
