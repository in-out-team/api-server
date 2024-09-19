package com.inout.fsrs.model

import java.time.Instant

data class RescheduleOptions(
    val enableFuzz: Boolean? = null,
    val dateHandler: ((Instant) -> Instant)? = null,
)
