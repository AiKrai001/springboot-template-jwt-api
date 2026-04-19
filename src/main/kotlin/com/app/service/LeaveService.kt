package com.app.service

import com.app.data.dto.LeaveApprovalActionRequest
import com.app.data.dto.LeaveRequestDetailView
import com.app.data.dto.LeaveRequestView
import com.app.data.dto.LeaveApplyRequest
import com.app.data.dto.LeaveTodoTaskView

interface LeaveService {
  fun apply(request: LeaveApplyRequest): LeaveRequestView

  fun myRequests(): List<LeaveRequestView>

  fun todoTasks(): List<LeaveTodoTaskView>

  fun approve(request: LeaveApprovalActionRequest): LeaveRequestView

  fun detail(leaveRequestId: Long): LeaveRequestDetailView
}
