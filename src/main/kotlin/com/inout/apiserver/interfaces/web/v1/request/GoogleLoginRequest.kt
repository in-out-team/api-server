package com.inout.apiserver.interfaces.web.v1.request

import com.inout.apiserver.base.enums.DeviceType

data class GoogleLoginRequest(
    val idToken: String,
    val deviceType: DeviceType,
)
