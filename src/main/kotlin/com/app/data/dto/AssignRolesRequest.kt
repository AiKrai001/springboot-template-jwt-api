package com.app.data.dto

data class AssignRolesRequest(
  val userId: Long,
  val roleIds: List<Long>
)
