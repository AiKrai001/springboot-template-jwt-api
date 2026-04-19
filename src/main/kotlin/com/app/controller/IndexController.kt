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
}
