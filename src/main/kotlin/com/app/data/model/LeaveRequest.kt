package com.app.data.model

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table
import java.math.BigDecimal
import java.sql.Timestamp

@Entity
@Table(name = "leave_request")
interface LeaveRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val leaveRequestId: Long

  val applicantId: Long
  val leaveType: String
  val leaveTimeSlot: String
  val startTime: Timestamp
  val endTime: Timestamp
  val durationHours: BigDecimal
  val reason: String?
  val status: String
  val configId: Long?
  val processDefinitionId: String?
  val processInstanceId: String?
  val currentTaskName: String?
  val createTime: Timestamp?
  val updateTime: Timestamp?
}
