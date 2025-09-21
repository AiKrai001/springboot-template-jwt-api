package com.app.controller

import cn.dev33.satoken.annotation.SaIgnore
import com.app.data.dto.AssignRolesRequest
import com.app.data.model.Role
import com.app.service.RoleService
import io.swagger.v3.oas.annotations.tags.Tag
import org.babyfish.jimmer.Page
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/role")
@Tag(name = "角色", description = "")
class RoleController(
  private val roleService: RoleService
) {
  @SaIgnore
  @PostMapping("/list")
  fun list(
    pageNum: Int? = 0,
    pageSize: Int? = 10
  ): Page<Role> {
    return roleService.list(pageNum!!, pageSize!!)
  }

  @SaIgnore
  @PostMapping("/create")
  fun create(
    @RequestBody role: Role
  ) {
    roleService.create(role)
  }

  @SaIgnore
  @PostMapping("/assign-to-user")
  fun assignToUser(
    @RequestBody req: AssignRolesRequest
  ) {
    roleService.addRolesToUser(req.userId, req.roleIds)
  }
}
