package com.dandd.templateparser.common

import java.util.UUID

sealed class DomainError(
    message: String,
) : Exception(message) {
    data class NotFound(
        val id: UUID,
    ) : DomainError("Resource with id $id not found")

    data class AlreadyExists(
        val field: String,
        val value: String,
    ) : DomainError("$field '$value' already exists")

    data class ValidationFailed(
        val errors: List<String>,
    ) : DomainError(errors.joinToString("; "))
}
