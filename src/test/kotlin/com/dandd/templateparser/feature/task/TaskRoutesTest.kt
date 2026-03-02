package com.dandd.templateparser.feature.task

import com.dandd.templateparser.config.configureRouting
import com.dandd.templateparser.config.configureSerialization
import com.dandd.templateparser.config.configureStatusPages
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.time.Instant

class TaskRoutesTest :
    DescribeSpec({

        val mockRepo = mockk<TaskRepository>()
        val testModule =
            module {
                single<TaskRepository> { mockRepo }
                single { TaskService(get()) }
            }

        fun Application.testModule() {
            install(Koin) { modules(testModule) }
            configureSerialization()
            configureStatusPages()
            configureRouting()
        }

        describe("GET /api/v1/tasks") {
            it("returns 200 with list of tasks") {
                testApplication {
                    application { testModule() }
                    val client =
                        createClient {
                            install(ContentNegotiation) { json() }
                        }

                    val taskId = TaskId.new()
                    val now = Instant.now()
                    coEvery { mockRepo.findAll() } returns
                        listOf(
                            Task(taskId, "Test Task", "Description", now, now),
                        )

                    val response = client.get("/api/v1/tasks")

                    response.status shouldBe HttpStatusCode.OK
                    val body = response.body<List<TaskResponse>>()
                    body.size shouldBe 1
                    body[0].title shouldBe "Test Task"
                }
            }
        }

        describe("GET /api/v1/tasks/{id}") {
            it("returns 200 when task found") {
                testApplication {
                    application { testModule() }
                    val client =
                        createClient {
                            install(ContentNegotiation) { json() }
                        }

                    val taskId = TaskId.new()
                    val now = Instant.now()
                    coEvery { mockRepo.findById(taskId) } returns
                        Task(taskId, "Found Task", null, now, now)

                    val response = client.get("/api/v1/tasks/${taskId.value}")

                    response.status shouldBe HttpStatusCode.OK
                    val body = response.body<TaskResponse>()
                    body.id shouldBe taskId.value.toString()
                }
            }

            it("returns 404 when task not found") {
                testApplication {
                    application { testModule() }
                    val client =
                        createClient {
                            install(ContentNegotiation) { json() }
                        }

                    val taskId = TaskId.new()
                    coEvery { mockRepo.findById(taskId) } returns null

                    val response = client.get("/api/v1/tasks/${taskId.value}")

                    response.status shouldBe HttpStatusCode.NotFound
                }
            }
        }

        describe("POST /api/v1/tasks") {
            it("returns 201 when request is valid") {
                testApplication {
                    application { testModule() }
                    val client =
                        createClient {
                            install(ContentNegotiation) { json() }
                        }

                    val request = CreateTaskRequest("New Task", "Description")
                    coEvery { mockRepo.existsByTitle(any()) } returns false
                    coEvery { mockRepo.save(any()) } answers { firstArg() }

                    val response =
                        client.post("/api/v1/tasks") {
                            contentType(ContentType.Application.Json)
                            setBody(request)
                        }

                    response.status shouldBe HttpStatusCode.Created
                }
            }

            it("returns 400 when title is blank") {
                testApplication {
                    application { testModule() }
                    val client =
                        createClient {
                            install(ContentNegotiation) { json() }
                        }

                    val request = CreateTaskRequest("", null)

                    val response =
                        client.post("/api/v1/tasks") {
                            contentType(ContentType.Application.Json)
                            setBody(request)
                        }

                    response.status shouldBe HttpStatusCode.BadRequest
                }
            }
        }

        describe("DELETE /api/v1/tasks/{id}") {
            it("returns 204 when task exists") {
                testApplication {
                    application { testModule() }
                    val client =
                        createClient {
                            install(ContentNegotiation) { json() }
                        }

                    val taskId = TaskId.new()
                    val now = Instant.now()
                    coEvery { mockRepo.findById(taskId) } returns
                        Task(taskId, "To Delete", null, now, now)
                    coEvery { mockRepo.deleteById(taskId) } returns true

                    val response = client.delete("/api/v1/tasks/${taskId.value}")

                    response.status shouldBe HttpStatusCode.NoContent
                }
            }

            it("returns 404 when task not found") {
                testApplication {
                    application { testModule() }
                    val client =
                        createClient {
                            install(ContentNegotiation) { json() }
                        }

                    val taskId = TaskId.new()
                    coEvery { mockRepo.findById(taskId) } returns null

                    val response = client.delete("/api/v1/tasks/${taskId.value}")

                    response.status shouldBe HttpStatusCode.NotFound
                }
            }
        }
    })
