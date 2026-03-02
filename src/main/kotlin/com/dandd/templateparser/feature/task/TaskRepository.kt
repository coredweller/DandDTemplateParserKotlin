package com.dandd.templateparser.feature.task

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface TaskRepository {
    suspend fun findById(id: TaskId): Task?

    suspend fun findAll(): List<Task>

    suspend fun save(task: Task): Task

    suspend fun existsByTitle(title: String): Boolean

    suspend fun deleteById(id: TaskId): Boolean
}

object TasksTable : Table("tasks") {
    val id = uuid("id")
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}

class TaskRepositoryImpl : TaskRepository {
    override suspend fun findById(id: TaskId): Task? =
        newSuspendedTransaction {
            TasksTable
                .selectAll()
                .where { TasksTable.id eq id.value }
                .singleOrNull()
                ?.toTask()
        }

    override suspend fun findAll(): List<Task> =
        newSuspendedTransaction {
            TasksTable.selectAll().map { it.toTask() }
        }

    override suspend fun save(task: Task): Task {
        newSuspendedTransaction {
            TasksTable.insert {
                it[id] = task.id.value
                it[title] = task.title
                it[description] = task.description
                it[createdAt] = task.createdAt
                it[updatedAt] = task.updatedAt
            }
        }
        return task
    }

    override suspend fun existsByTitle(title: String): Boolean =
        newSuspendedTransaction {
            TasksTable
                .selectAll()
                .where { TasksTable.title eq title }
                .count() > 0
        }

    override suspend fun deleteById(id: TaskId): Boolean =
        newSuspendedTransaction {
            val deleted = TasksTable.deleteWhere { TasksTable.id eq id.value }
            deleted > 0
        }

    private fun ResultRow.toTask() =
        Task(
            id = TaskId(this[TasksTable.id]),
            title = this[TasksTable.title],
            description = this[TasksTable.description],
            createdAt = this[TasksTable.createdAt],
            updatedAt = this[TasksTable.updatedAt],
        )
}
