package com.app.controller

import cn.dev33.satoken.annotation.SaCheckRole
import com.app.data.model.User
import com.app.service.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import org.babyfish.jimmer.Page
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户", description = "")
class UserController(
  private val userService: UserService
) {

  @SaCheckRole("admin")
  @PostMapping("/list")
  fun list(
    pageNum: Int? = 1,
    pageSize: Int? = 10
  ): Page<User> {
    return userService.list(pageNum!!, pageSize!!)
  }
}
