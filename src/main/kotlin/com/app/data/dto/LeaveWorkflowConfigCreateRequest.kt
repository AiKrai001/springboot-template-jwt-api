package com.app.data.dto

data class LeaveWorkflowConfigCreateRequest(
  val configName: String,
  val priority: Int = 0,
  val enabled: Boolean = true,
  val condition: LeaveWorkflowCondition,
  val nodes: List<LeaveWorkflowNode>,
  val remark: String? = null
)
