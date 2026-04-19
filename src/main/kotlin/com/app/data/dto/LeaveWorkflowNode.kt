package com.app.data.dto

data class LeaveWorkflowNode(
  val nodeName: String,
  val approverRoleKeys: List<String>,
  val parallel: Boolean = false
)
