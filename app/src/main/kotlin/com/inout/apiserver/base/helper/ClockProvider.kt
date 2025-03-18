package com.inout.apiserver.base.helper

import java.time.Clock
import java.time.Instant

object ClockProvider {
    private var clock: Clock = Clock.systemUTC()

    fun now(): Instant = clock.instant()
}
