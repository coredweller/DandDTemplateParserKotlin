package com.dandd.templateparser.feature.task

import java.time.Instant
import java.util.UUID

@JvmInline
value class TaskId(
    val value: UUID,
) {
    companion object {
        fun new() = TaskId(UUID.randomUUID())

        fun from(value: String) = TaskId(UUID.fromString(value))
    }
}

data class Task(
    val id: TaskId,
    val title: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
