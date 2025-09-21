package com.app.data.model

import org.babyfish.jimmer.sql.*
import java.sql.Timestamp

@Entity
@Table(name = "sys_role")
interface Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val roleId: Long

  val roleName: String

  @Column(name = "role_key")
  val roleKey: String

  val status: String?

  val remark: String?

  val createTime: Timestamp?
  val updateTime: Timestamp?

  @ManyToMany(mappedBy = "roles")
  val users: List<User>

  @ManyToMany
  @JoinTable(
    name = "sys_role_permission",
    joinColumnName = "role_id",
    inverseJoinColumnName = "permission_id"
  )
  val permissions: List<Permission>
}
