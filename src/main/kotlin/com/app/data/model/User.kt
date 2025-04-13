package com.app.data.model

import org.babyfish.jimmer.sql.*
import java.sql.Timestamp

@Entity
@Table(name = "sys_user")
interface User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val userId: Long

  val deptId: Long?

  @Column(name = "user_name")
  val userName: String

  val nickName: String?

  val userType: String?

  val email: String?
  val phone: String?
  val sex: String?
  val avatar: String?
  val password: String?
  val status: String?
  val delFlag: String?
  val loginIp: String?
  val loginDate: Timestamp?
  val createBy: String?
  val createTime: Timestamp?
  val updateBy: String?
  val updateTime: Timestamp?
  val remark: String?
}