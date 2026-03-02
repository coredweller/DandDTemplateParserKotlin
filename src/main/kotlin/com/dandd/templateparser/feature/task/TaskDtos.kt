package com.dandd.templateparser.feature.task

import io.konform.validation.Validation
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.minLength
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
) {
    companion object {
        val validate =
            Validation {
                CreateTaskRequest::title {
                    minLength(1) hint "Title must not be blank"
                    maxLength(255) hint "Title must not exceed 255 characters"
                }
            }
    }
}

@Serializable
data class TaskResponse(
    val id: String,
    val title: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String,
)

fun Task.toResponse() =
    TaskResponse(
        id = id.value.toString(),
        title = title,
        description = description,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
