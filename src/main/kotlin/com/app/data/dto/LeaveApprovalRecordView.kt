package com.app.data.dto

import java.sql.Timestamp

data class LeaveApprovalRecordView(
  val recordId: Long,
  val taskId: String,
  val taskName: String,
  val action: String,
  val approverId: Long,
  val approverName: String,
  val comment: String?,
  val createTime: Timestamp?
)
