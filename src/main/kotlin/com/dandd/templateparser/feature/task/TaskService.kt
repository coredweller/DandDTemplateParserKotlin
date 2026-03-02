package com.dandd.templateparser.feature.task

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.dandd.templateparser.common.DomainError
import org.slf4j.LoggerFactory
import java.time.Instant

class TaskService(
    private val repo: TaskRepository,
) {
    private val logger = LoggerFactory.getLogger(TaskService::class.java)

    suspend fun findById(id: TaskId): Either<DomainError, Task> {
        logger.debug("Finding task by id={}", id.value)
        return repo.findById(id)?.right()
            ?: DomainError.NotFound(id.value).left()
    }

    suspend fun findAll(): List<Task> = repo.findAll()

    suspend fun create(request: CreateTaskRequest): Either<DomainError, Task> {
        val validationResult = CreateTaskRequest.validate(request)
        if (validationResult.errors.isNotEmpty()) {
            val messages = validationResult.errors.map { it.message }
            logger.warn("Validation failed for create task: {}", messages)
            return DomainError.ValidationFailed(messages).left()
        }

        if (repo.existsByTitle(request.title)) {
            logger.warn("Task with title='{}' already exists", request.title)
            return DomainError.AlreadyExists("title", request.title).left()
        }

        val now = Instant.now()
        val task =
            Task(
                id = TaskId.new(),
                title = request.title,
                description = request.description,
                createdAt = now,
                updatedAt = now,
            )
        return repo
            .save(task)
            .also {
                logger.info("Created task id={}", task.id.value)
            }.right()
    }

    suspend fun delete(id: TaskId): Either<DomainError, Unit> {
        logger.debug("Deleting task by id={}", id.value)
        val exists = repo.findById(id)
        if (exists == null) {
            return DomainError.NotFound(id.value).left()
        }
        repo.deleteById(id)
        logger.info("Deleted task id={}", id.value)
        return Unit.right()
    }
}
