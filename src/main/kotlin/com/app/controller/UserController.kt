package com.app.controller

import cn.dev33.satoken.annotation.SaIgnore
import com.app.data.model.User
import com.app.service.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import org.babyfish.jimmer.Page
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户", description = "")
class UserController(
  private val userService: UserService
) {

  //  @SaCheckRole("admin")
  @SaIgnore
  @PostMapping("/list")
  fun list(
    pageNum: Int? = 0,
    pageSize: Int? = 10
  ): Page<User> {
    return userService.list(pageNum!!, pageSize!!)
  }

  @SaIgnore
  @PostMapping("/create")
  fun create(
    @RequestBody user: User
  ) {
    userService.create(user)
  }
}
