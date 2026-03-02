package com.dandd.templateparser.feature.sheet

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.sheetRoutes(service: SheetService) {
    route("/api/v1/sheets") {
        get {
            val type = call.request.queryParameters["type"]
            val levelParam = call.request.queryParameters["level"]
            val level = if (levelParam != null) {
                levelParam.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
            } else {
                null
            }
            call.respond(service.findSummaries(type, level).map { it.toResponse() })
        }

        get("/{id}") {
            val rawId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val id = try {
                SheetId.from(rawId)
            } catch (e: IllegalArgumentException) {
                return@get call.respond(HttpStatusCode.BadRequest)
            }
            service.findById(id).fold(
                ifLeft = { error -> throw error },
                ifRight = { html -> call.respondText(html, ContentType.Text.Html) },
            )
        }

        post("/general") {
            val request = call.receive<GeneralSheetRequest>()
            service.renderGeneral(request).fold(
                ifLeft = { error -> throw error },
                ifRight = { html -> call.respondText(html, ContentType.Text.Html, HttpStatusCode.Created) },
            )
        }

        post("/legendary") {
            val request = call.receive<LegendarySheetRequest>()
            service.renderLegendary(request).fold(
                ifLeft = { error -> throw error },
                ifRight = { html -> call.respondText(html, ContentType.Text.Html, HttpStatusCode.Created) },
            )
        }
    }
}
