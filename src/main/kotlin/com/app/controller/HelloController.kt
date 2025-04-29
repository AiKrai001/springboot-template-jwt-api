package com.app.controller

import cn.dev33.satoken.annotation.SaIgnore
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class HelloController {

  @SaIgnore
  @GetMapping("/")
  fun hello(): String {
    return "Hello, World!"
  }
}
