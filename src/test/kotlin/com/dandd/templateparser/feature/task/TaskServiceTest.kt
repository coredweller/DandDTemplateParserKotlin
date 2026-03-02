package com.dandd.templateparser.feature.task

import com.dandd.templateparser.common.DomainError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant

class TaskServiceTest :
    DescribeSpec({

        val repo = mockk<TaskRepository>()
        val service = TaskService(repo)

        beforeEach {
            clearMocks(repo)
        }

        describe("findById") {
            context("when task exists") {
                val taskId = TaskId.new()
                val now = Instant.now()
                val task =
                    Task(
                        id = taskId,
                        title = "Sample Task",
                        description = "A test task",
                        createdAt = now,
                        updatedAt = now,
                    )

                beforeEach {
                    coEvery { repo.findById(taskId) } returns task
                }

                it("returns Right(Task)") {
                    val result = service.findById(taskId)
                    val found = result.shouldBeRight()
                    found.title shouldBe "Sample Task"
                }
            }

            context("when task does not exist") {
                val taskId = TaskId.new()

                beforeEach {
                    coEvery { repo.findById(taskId) } returns null
                }

                it("returns Left(NotFound)") {
                    val result = service.findById(taskId)
                    val error = result.shouldBeLeft()
                    error shouldBe DomainError.NotFound(taskId.value)
                }
            }
        }

        describe("create") {
            val validRequest =
                CreateTaskRequest(
                    title = "New Task",
                    description = "Task description",
                )

            context("when title is not taken") {
                beforeEach {
                    coEvery { repo.existsByTitle(validRequest.title) } returns false
                    coEvery { repo.save(any()) } answers { firstArg() }
                }

                it("returns Right(Task) and saves to repository") {
                    val result = service.create(validRequest)
                    val created = result.shouldBeRight()
                    created.title shouldBe validRequest.title
                    coVerify(exactly = 1) { repo.save(any()) }
                }
            }

            context("when title is already taken") {
                beforeEach {
                    coEvery { repo.existsByTitle(validRequest.title) } returns true
                }

                it("returns Left(AlreadyExists) and save is NOT called") {
                    val result = service.create(validRequest)
                    val error = result.shouldBeLeft()
                    error shouldBe DomainError.AlreadyExists("title", validRequest.title)
                    coVerify(exactly = 0) { repo.save(any()) }
                }
            }

            context("when request is invalid") {
                val invalidRequest = CreateTaskRequest(title = "", description = null)

                it("returns Left(ValidationFailed) without touching repository") {
                    val result = service.create(invalidRequest)
                    val error = result.shouldBeLeft()
                    (error as DomainError.ValidationFailed).errors.isNotEmpty() shouldBe true
                    coVerify(exactly = 0) { repo.existsByTitle(any()) }
                }
            }
        }

        describe("delete") {
            context("when task exists") {
                val taskId = TaskId.new()
                val now = Instant.now()
                val task = Task(taskId, "To Delete", null, now, now)

                beforeEach {
                    coEvery { repo.findById(taskId) } returns task
                    coEvery { repo.deleteById(taskId) } returns true
                }

                it("returns Right(Unit)") {
                    val result = service.delete(taskId)
                    result.shouldBeRight()
                    coVerify(exactly = 1) { repo.deleteById(taskId) }
                }
            }

            context("when task does not exist") {
                val taskId = TaskId.new()

                beforeEach {
                    coEvery { repo.findById(taskId) } returns null
                }

                it("returns Left(NotFound)") {
                    val result = service.delete(taskId)
                    val error = result.shouldBeLeft()
                    error shouldBe DomainError.NotFound(taskId.value)
                }
            }
        }
    })
