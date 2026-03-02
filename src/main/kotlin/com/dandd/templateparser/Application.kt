package com.dandd.templateparser

import com.dandd.templateparser.config.configureDI
import com.dandd.templateparser.config.configureDatabase
import com.dandd.templateparser.config.configureMonitoring
import com.dandd.templateparser.config.configureRouting
import com.dandd.templateparser.config.configureSerialization
import com.dandd.templateparser.config.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureDI()
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureDatabase()
    configureRouting()
}
