package com.app.service.impl

import cn.dev33.satoken.stp.StpUtil
import cn.dev33.satoken.stp.parameter.SaLoginParameter
import cn.hutool.crypto.SecureUtil
import com.app.data.model.User
import com.app.repository.UserRepository
import com.app.service.UserService
import org.babyfish.jimmer.Page
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
  private val userRepository: UserRepository
) : UserService {
  override fun signIn(username: String, password: String): String {
    val user = userRepository.findByUserName(username) ?: throw Exception("User not found")
    if (user.password != SecureUtil.sha1(password)) {
      throw Exception("Invalid password")
    }
    val parameter = SaLoginParameter.create()
      .setExtraData(mapOf("deptId" to user.deptId))
    StpUtil.login(user.userId.toString(), parameter)
    val token = StpUtil.getTokenInfo()
    return token.tokenValue
  }

  override fun list(
    pageNum: Int,
    pageSize: Int
  ): Page<User> {
    return userRepository.findUser(pageNum, pageSize)
  }
}
