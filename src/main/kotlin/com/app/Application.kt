package com.app

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringbootTemplateJwtApiApplication

private val log = LoggerFactory.getLogger(SpringbootTemplateJwtApiApplication::class.java)

fun main(args: Array<String>) {
  runApplication<SpringbootTemplateJwtApiApplication>(*args).let {
    log.info("🚀 启动成功!")
    log.info("📖 访问地址: http://localhost:18080")
  }
}
