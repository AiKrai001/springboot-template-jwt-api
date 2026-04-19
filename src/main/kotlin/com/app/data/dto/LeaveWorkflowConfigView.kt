package com.app.data.dto

import java.sql.Timestamp

data class LeaveWorkflowConfigView(
  val configId: Long,
  val configName: String,
  val priority: Int,
  val enabled: Boolean,
  val condition: LeaveWorkflowCondition,
  val nodes: List<LeaveWorkflowNode>,
  val processKey: String?,
  val processDefinitionId: String?,
  val remark: String?,
  val createTime: Timestamp?,
  val updateTime: Timestamp?
)
