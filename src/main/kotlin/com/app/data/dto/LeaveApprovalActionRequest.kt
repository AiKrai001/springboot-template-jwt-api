package com.app.data.dto

data class LeaveApprovalActionRequest(
  val taskId: String,
  val approved: Boolean,
  val comment: String? = null
)
