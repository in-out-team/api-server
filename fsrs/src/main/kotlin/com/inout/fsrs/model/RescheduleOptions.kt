package com.inout.fsrs.model

import java.util.*

data class RescheduleOptions(
    val enableFuzz: Boolean? = null,
    val dateHandler: ((Date) -> Date)? = null,
)
