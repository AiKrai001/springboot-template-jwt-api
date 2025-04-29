package com.app.service

import com.app.data.model.User
import org.babyfish.jimmer.Page

interface UserService {
  fun signIn(username: String, password: String): String

  fun list(
    pageNum: Int = 0,
    pageSize: Int = 10
  ): Page<User>
}
