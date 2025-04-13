package com.app.data.dto

import io.swagger.v3.oas.annotations.media.Schema

data class SignInRequest (
  @Schema(title = "username", description = "账号名称", defaultValue = "")
  var username: String,
  @Schema(title = "password", description = "账号密码", defaultValue = "")
  var password: String
)