package com.app.controller

import cn.dev33.satoken.annotation.SaCheckRole
import com.app.data.dto.LeaveWorkflowConfigCreateRequest
import com.app.data.dto.LeaveWorkflowConfigView
import com.app.service.LeaveWorkflowConfigService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/leave-config")
@Tag(name = "请假审批流配置", description = "")
class LeaveWorkflowConfigController(
  private val leaveWorkflowConfigService: LeaveWorkflowConfigService
) {
  @SaCheckRole("admin")
  @PostMapping("/create")
  fun create(
    @RequestBody request: LeaveWorkflowConfigCreateRequest
  ): LeaveWorkflowConfigView {
    return leaveWorkflowConfigService.create(request)
  }

  @SaCheckRole("admin")
  @PostMapping("/list")
  fun list(): List<LeaveWorkflowConfigView> {
    return leaveWorkflowConfigService.list()
  }
}
