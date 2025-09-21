package com.app.service.impl

import com.app.data.model.Role
import com.app.data.model.User
import com.app.repository.RoleRepository
import com.app.service.RoleService
import org.babyfish.jimmer.Page
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class RoleServiceImpl(
  private val roleRepository: RoleRepository,
  private val sqlClient: KSqlClient
) : RoleService {
  override fun list(pageNum: Int, pageSize: Int): Page<Role> {
    return roleRepository.findRole(pageNum, pageSize)
  }

  override fun create(role: Role) {
    roleRepository.save(role)
  }

  override fun findRoleKeysByUserId(userId: Long): List<String> {
    return roleRepository.findRoleKeysByUserId(userId)
  }

  override fun addRolesToUser(userId: Long, roleIds: List<Long>) {
    roleIds.forEach { rid ->
      sqlClient.getAssociations(User::roles).save(userId, rid)
    }
  }
}
