package com.app.repository

import com.app.data.model.*
import org.babyfish.jimmer.Page
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.`eq?`

interface RoleRepository : KRepository<Role, Long> {

  fun findRole(
    pageIndex: Int = 0,
    pageSize: Int = 10,
    id: Long? = null,
    name: String? = null,
    key: String? = null
  ): Page<Role> =
    sql.createQuery(Role::class) {
      where(table.roleId `eq?` id)
      where(table.roleName `eq?` name)
      where(table.roleKey `eq?` key)
      select(table)
    }.fetchPage(pageIndex, pageSize)

  fun findRoleKeysByUserId(uId: Long): List<String> =
    sql.createQuery(Role::class) {
      where += table.users { userId eq uId }
      select(table.roleKey)
    }.execute()
}
