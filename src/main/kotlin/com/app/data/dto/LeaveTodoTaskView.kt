package com.app.data.dto

import java.math.BigDecimal
import java.util.Date

data class LeaveTodoTaskView(
  val taskId: String,
  val taskName: String,
  val processInstanceId: String,
  val leaveRequestId: Long,
  val applicantId: Long,
  val applicantName: String?,
  val leaveType: String,
  val leaveTimeSlot: String,
  val durationHours: BigDecimal,
  val reason: String?,
  val createdTime: Date?
)
