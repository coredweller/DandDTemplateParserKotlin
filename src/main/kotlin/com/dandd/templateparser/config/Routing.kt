package com.dandd.templateparser.config

import com.dandd.templateparser.feature.sheet.SheetService
import com.dandd.templateparser.feature.sheet.sheetRoutes
import com.dandd.templateparser.feature.task.TaskService
import com.dandd.templateparser.feature.task.taskRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val taskService: TaskService by inject()
    val sheetService: SheetService by inject()

    routing {
        healthRoutes()
        taskRoutes(taskService)
        sheetRoutes(sheetService)
    }
}
