package com.dandd.templateparser.config

import com.dandd.templateparser.common.DomainError
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StatusPages")

@Serializable
data class ProblemDetail(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<DomainError.NotFound> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ProblemDetail("not-found", "Resource Not Found", 404, cause.message ?: "Not found"),
            )
        }
        exception<DomainError.AlreadyExists> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ProblemDetail("conflict", "Resource Already Exists", 409, cause.message ?: "Conflict"),
            )
        }
        exception<DomainError.ValidationFailed> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ProblemDetail("validation-error", "Validation Failed", 400, cause.message ?: "Bad request"),
            )
        }
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ProblemDetail("internal-error", "Internal Server Error", 500, "An unexpected error occurred"),
            )
        }
    }
}
