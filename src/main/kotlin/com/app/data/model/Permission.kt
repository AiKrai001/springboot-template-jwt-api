package com.app.data.model

import org.babyfish.jimmer.sql.*
import java.sql.Timestamp

@Entity
@Table(name = "sys_permission")
interface Permission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val permissionId: Long

  @Column(name = "perm_name")
  val permName: String

  @Column(name = "perm_key")
  val permKey: String

  val status: String?
  val remark: String?
  val createTime: Timestamp?
  val updateTime: Timestamp?

  @ManyToMany(mappedBy = "permissions")
  val roles: List<Role>
}
