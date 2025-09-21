package com.app.controller

import cn.dev33.satoken.annotation.SaIgnore
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IndexController {

  @SaIgnore
  @RequestMapping("/")
  fun index(): String {
    return "Hello, World!"
  }

  /**
   * 处理浏览器自动请求的favicon.ico，避免触发全局异常处理
   */
  @SaIgnore
  @RequestMapping("/favicon.ico")
  fun favicon(): ResponseEntity<Void?> {
    // 返回204 No Content，避免404错误
    return ResponseEntity.noContent().build<Void?>()
  }
}
