package com.app.data.dto

data class AssignPermissionsRequest(
  val roleId: Long,
  val permissionIds: List<Long>
)
