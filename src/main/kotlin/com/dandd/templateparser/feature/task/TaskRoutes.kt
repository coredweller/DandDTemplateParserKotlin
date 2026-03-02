package com.dandd.templateparser.feature.task

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.taskRoutes(service: TaskService) {
    route("/api/v1/tasks") {
        get {
            val tasks = service.findAll()
            call.respond(tasks.map { it.toResponse() })
        }

        get("/{id}") {
            val id =
                TaskId.from(
                    call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest),
                )
            service.findById(id).fold(
                ifLeft = { error -> throw error },
                ifRight = { task -> call.respond(task.toResponse()) },
            )
        }

        post {
            val request = call.receive<CreateTaskRequest>()
            service.create(request).fold(
                ifLeft = { error -> throw error },
                ifRight = { task -> call.respond(HttpStatusCode.Created, task.toResponse()) },
            )
        }

        delete("/{id}") {
            val id =
                TaskId.from(
                    call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest),
                )
            service.delete(id).fold(
                ifLeft = { error -> throw error },
                ifRight = { call.respond(HttpStatusCode.NoContent) },
            )
        }
    }
}
