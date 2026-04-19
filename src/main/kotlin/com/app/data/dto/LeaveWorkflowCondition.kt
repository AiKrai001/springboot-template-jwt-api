package com.app.data.dto

import java.math.BigDecimal

data class LeaveWorkflowCondition(
  val leaveTypes: List<String> = emptyList(),
  val minDurationHours: BigDecimal? = null,
  val maxDurationHours: BigDecimal? = null,
  val timeSlots: List<String> = emptyList()
)
