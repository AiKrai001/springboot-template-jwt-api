package com.app.controller

import cn.dev33.satoken.annotation.SaIgnore
import com.app.data.dto.AssignPermissionsRequest
import com.app.data.model.Permission
import com.app.service.PermissionService
import io.swagger.v3.oas.annotations.tags.Tag
import org.babyfish.jimmer.Page
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/permission")
@Tag(name = "权限", description = "")
class PermissionController(
  private val permissionService: PermissionService
) {
  @SaIgnore
  @PostMapping("/list")
  fun list(
    pageNum: Int? = 0,
    pageSize: Int? = 10
  ): Page<Permission> {
    return permissionService.list(pageNum!!, pageSize!!)
  }

  @SaIgnore
  @PostMapping("/create")
  fun create(
    @RequestBody permission: Permission
  ) {
    permissionService.create(permission)
  }

  @SaIgnore
  @PostMapping("/assign-to-role")
  fun assignToRole(
    @RequestBody req: AssignPermissionsRequest
  ) {
    permissionService.addPermissionsToRole(req.roleId, req.permissionIds)
  }
}
