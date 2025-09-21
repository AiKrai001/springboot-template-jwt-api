package com.app.service

import com.app.data.model.Role
import org.babyfish.jimmer.Page

interface RoleService {
  fun list(pageNum: Int = 0, pageSize: Int = 10): Page<Role>
  fun create(role: Role)
  fun findRoleKeysByUserId(userId: Long): List<String>
  fun addRolesToUser(userId: Long, roleIds: List<Long>)
}
