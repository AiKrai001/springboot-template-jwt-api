package com.app.service

import com.app.data.model.Permission
import org.babyfish.jimmer.Page

interface PermissionService {
  fun list(pageNum: Int = 0, pageSize: Int = 10): Page<Permission>
  fun create(permission: Permission)
  fun findPermissionKeysByUserId(userId: Long): List<String>
  fun addPermissionsToRole(roleId: Long, permissionIds: List<Long>)
}
