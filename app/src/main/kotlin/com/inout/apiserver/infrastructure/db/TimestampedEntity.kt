package com.inout.apiserver.infrastructure.db

import com.inout.apiserver.config.jpa.JpaAuditingConfig
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@MappedSuperclass
@EntityListeners(JpaAuditingConfig::class)
abstract class TimestampedEntity {
    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "timestamp(9) with time zone")
    var createdAt: Instant? = null

    @UpdateTimestamp
    @InstantColumn
    var updatedAt: Instant? = null
}
