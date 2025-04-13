package com.app.controller;

import cn.dev33.satoken.stp.StpUtil
import com.app.data.dto.SignInRequest
import com.app.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name = "登录校验相关", description = "包括用户登录、注册、验证码请求等操作。")
class AuthorizeController(
  private val userService: UserService
) {
  @Operation(summary = "注册")
  @PostMapping("/register")
  fun register(
    @RequestBody signInRequest: SignInRequest
  ): String {
    return userService.signIn(signInRequest.username, signInRequest.password)
  }

  @Operation(summary = "登录")
  @PostMapping("/login")
  fun login(
    @RequestBody signInRequest: SignInRequest
  ): String {
    return userService.signIn(signInRequest.username, signInRequest.password)
  }

  @Operation(summary = "退出登录")
  @PostMapping("/logout")
  fun logout(): String {
    StpUtil.logout()
    return "退出登录成功"
  }
}
