package com.app.repository

import com.app.data.model.*
import org.babyfish.jimmer.Page
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.`eq?`

interface PermissionRepository : KRepository<Permission, Long> {

  fun findPermission(
    pageIndex: Int = 0,
    pageSize: Int = 10,
    id: Long? = null,
    name: String? = null,
    key: String? = null
  ): Page<Permission> =
    sql.createQuery(Permission::class) {
      where(table.permissionId `eq?` id)
      where(table.permName `eq?` name)
      where(table.permKey `eq?` key)
      select(table)
    }.fetchPage(pageIndex, pageSize)

  fun findPermissionKeysByUserId(uId: Long): List<String> =
    sql.createQuery(Permission::class) {
      where += table.roles { users { userId eq uId } }
      select(table.permKey)
    }.execute()
}
