package com.app.config.satoken

import cn.dev33.satoken.stp.StpInterface
import com.app.service.PermissionService
import com.app.service.RoleService
import com.app.service.UserService
import org.springframework.stereotype.Component

/**
 * 自定义权限校验接口扩展
 */
@Component
class StpInterfaceImpl(
  private val userService: UserService,
  private val roleService: RoleService,
  private val permissionService: PermissionService
) : StpInterface {
  /**
   * 返回一个账号所拥有的权限码集合
   */
  override fun getPermissionList(loginId: Any, loginType: String): List<String> =
    try {
      val uid = loginId.toString().toLong()
      permissionService.findPermissionKeysByUserId(uid)
    } catch (_: Exception) {
      emptyList()
    }

  /**
   * 返回一个账号所拥有的角色标识集合
   */
  override fun getRoleList(loginId: Any, loginType: String): List<String> =
    try {
      val uid = loginId.toString().toLong()
      roleService.findRoleKeysByUserId(uid)
    } catch (_: Exception) {
      emptyList()
    }
}
