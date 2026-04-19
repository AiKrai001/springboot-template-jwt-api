package com.app

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringbootTemplateJwtApiApplication

private val log = LoggerFactory.getLogger(SpringbootTemplateJwtApiApplication::class.java)

fun main(args: Array<String>) {
  runApplication<SpringbootTemplateJwtApiApplication>(*args).let {
    log.atInfo()
      .addKeyValue("event.action", "application.started")
      .addKeyValue("server.port", 18080)
      .log("Application started")
  }
}
