package com.app.data.model

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table
import java.sql.Timestamp

@Entity
@Table(name = "leave_approval_record")
interface LeaveApprovalRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val recordId: Long

  val leaveRequestId: Long
  val taskId: String
  val taskName: String
  val action: String
  val approverId: Long
  val approverName: String
  val comment: String?
  val createTime: Timestamp?
}
