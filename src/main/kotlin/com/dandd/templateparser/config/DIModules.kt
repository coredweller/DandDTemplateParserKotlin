package com.dandd.templateparser.config

import com.dandd.templateparser.feature.sheet.SheetRepository
import com.dandd.templateparser.feature.sheet.SheetRepositoryImpl
import com.dandd.templateparser.feature.sheet.SheetService
import com.dandd.templateparser.feature.task.TaskRepository
import com.dandd.templateparser.feature.task.TaskRepositoryImpl
import com.dandd.templateparser.feature.task.TaskService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

val appModule =
    module {
        single<TaskRepository> { TaskRepositoryImpl() }
        single { TaskService(get()) }
        single<SheetRepository> { SheetRepositoryImpl() }
        single { SheetService(get()) }
    }

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}
