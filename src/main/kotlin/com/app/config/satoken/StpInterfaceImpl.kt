package com.app.config.satoken

import cn.dev33.satoken.stp.StpInterface
import com.app.service.UserService
import org.springframework.stereotype.Component

/**
 * 自定义权限验证接口扩展
 */
@Component // 打开此注解，保证此类被springboot扫描，即可完成sa-token的自定义权限验证扩展
class StpInterfaceImpl(
  private val userService: UserService
) : StpInterface {
  /**
   * 返回一个账号所拥有的权限码集合
   */
  override fun getPermissionList(loginId: Any, loginType: String): List<String> {
    // 本list仅做模拟，实际项目中要根据具体业务逻辑来查询权限
//    userService.selectById(loginId as Long)

    return listOf(
      "101",
      "user-add",
      "user-delete",
      "user-update",
      "user-get",
      "article-get"
    )
  }

  /**
   * 返回一个账号所拥有的角色标识集合
   */
  override fun getRoleList(loginId: Any, loginType: String): List<String> {
    // 本list仅做模拟，实际项目中要根据具体业务逻辑来查询角色
    return listOf("admin", "super-admin")

    // 实际业务代码示例:
    // val id = loginId as Long
    // val userEntity = userMapper.selectById(id)
    // return listOf(userEntity.role)
  }
}
