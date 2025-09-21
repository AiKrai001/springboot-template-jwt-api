package com.app.service.impl

import com.app.data.model.Permission
import com.app.data.model.Role
import com.app.repository.PermissionRepository
import com.app.service.PermissionService
import org.babyfish.jimmer.Page
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class PermissionServiceImpl(
  private val permissionRepository: PermissionRepository,
  private val sqlClient: KSqlClient
) : PermissionService {
  override fun list(pageNum: Int, pageSize: Int): Page<Permission> {
    return permissionRepository.findPermission(pageNum, pageSize)
  }

  override fun create(permission: Permission) {
    permissionRepository.save(permission)
  }

  override fun findPermissionKeysByUserId(userId: Long): List<String> {
    return permissionRepository.findPermissionKeysByUserId(userId)
  }

  override fun addPermissionsToRole(roleId: Long, permissionIds: List<Long>) {
    permissionIds.forEach { pid ->
      sqlClient.getAssociations(Role::permissions).save(roleId, pid)
    }
  }
}
