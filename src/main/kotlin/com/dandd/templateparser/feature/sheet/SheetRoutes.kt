package com.dandd.templateparser.feature.sheet

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.sheetRoutes(service: SheetService) {
    route("/api/v1/sheets") {
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
