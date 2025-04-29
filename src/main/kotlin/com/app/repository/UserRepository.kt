package com.app.repository

import com.app.data.model.User
import com.app.data.model.userId
import com.app.data.model.userName
import org.babyfish.jimmer.Page
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.`eq?`

interface UserRepository : KRepository<User, Long> {
  fun findUser(
    pageIndex: Int = 0,
    pageSize: Int = 10,
    id: Long? = null,
    name: String? = null,
    email: String? = null,
    phone: String? = null,
  ): Page<User> =
    sql.createQuery(User::class) {
      where(table.userId `eq?` id)
      select(table)
    }.fetchPage(pageIndex, pageSize)

  fun findByUserName(username: String): User? = sql.createQuery(User::class) {
    where(table.userName eq username)
    select(table)
  }.fetchOne()
}
