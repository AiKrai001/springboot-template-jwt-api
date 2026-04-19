package com.app.data.dto

import java.time.LocalDateTime

data class LeaveApplyRequest(
  val leaveType: String,
  val leaveTimeSlot: String,
  val startTime: LocalDateTime,
  val endTime: LocalDateTime,
  val reason: String? = null
)
