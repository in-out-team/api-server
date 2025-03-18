package com.inout.apiserver.infrastructure.db

import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@AttributeOverrides(
    AttributeOverride(name = "columnDefinition", column = Column(columnDefinition = "timestamp(9) with time zone")),
)
annotation class InstantColumn
