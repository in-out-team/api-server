package com.inout.fsrs.model

import com.inout.fsrs.model.enums.Grade

data class RecordLog(
    val logs: Map<Grade, RecordLogItem>
)
