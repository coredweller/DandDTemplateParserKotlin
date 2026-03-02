package com.dandd.templateparser.config

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: String,
)

fun Route.healthRoutes() {
    get("/api/v1/health") {
        call.respond(HealthResponse(status = "ok", timestamp = Instant.now().toString()))
    }
}
