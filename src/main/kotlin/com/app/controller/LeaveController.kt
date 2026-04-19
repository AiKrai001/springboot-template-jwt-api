package com.app.controller

import com.app.data.dto.LeaveApprovalActionRequest
import com.app.data.dto.LeaveRequestDetailView
import com.app.data.dto.LeaveRequestView
import com.app.data.dto.LeaveApplyRequest
import com.app.data.dto.LeaveTodoTaskView
import com.app.service.LeaveService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/leave")
@Tag(name = "请假审批", description = "")
class LeaveController(
  private val leaveService: LeaveService
) {
  @PostMapping("/apply")
  fun apply(
    @RequestBody request: LeaveApplyRequest
  ): LeaveRequestView {
    return leaveService.apply(request)
  }

  @PostMapping("/my-list")
  fun myList(): List<LeaveRequestView> {
    return leaveService.myRequests()
  }

  @PostMapping("/todo-tasks")
  fun todoTasks(): List<LeaveTodoTaskView> {
    return leaveService.todoTasks()
  }

  @PostMapping("/approve")
  fun approve(
    @RequestBody request: LeaveApprovalActionRequest
  ): LeaveRequestView {
    return leaveService.approve(request)
  }

  @PostMapping("/detail")
  fun detail(
    @RequestParam leaveRequestId: Long
  ): LeaveRequestDetailView {
    return leaveService.detail(leaveRequestId)
  }
}
