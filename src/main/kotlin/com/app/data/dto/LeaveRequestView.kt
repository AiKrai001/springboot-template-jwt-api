package com.app.data.dto

import java.math.BigDecimal
import java.sql.Timestamp

data class LeaveRequestView(
  val leaveRequestId: Long,
  val applicantId: Long,
  val applicantName: String?,
  val leaveType: String,
  val leaveTimeSlot: String,
  val startTime: Timestamp,
  val endTime: Timestamp,
  val durationHours: BigDecimal,
  val reason: String?,
  val status: String,
  val configId: Long?,
  val processDefinitionId: String?,
  val processInstanceId: String?,
  val currentTaskName: String?,
  val createTime: Timestamp?,
  val updateTime: Timestamp?
)
