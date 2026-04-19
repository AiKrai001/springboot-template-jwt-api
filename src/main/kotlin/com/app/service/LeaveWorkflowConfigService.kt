package com.app.service

import com.app.data.dto.LeaveWorkflowConfigCreateRequest
import com.app.data.dto.LeaveWorkflowConfigView
import com.app.data.model.LeaveWorkflowConfig
import java.math.BigDecimal

interface LeaveWorkflowConfigService {
  fun create(request: LeaveWorkflowConfigCreateRequest): LeaveWorkflowConfigView

  fun list(): List<LeaveWorkflowConfigView>

  fun resolveMatchedConfig(
    leaveType: String,
    durationHours: BigDecimal,
    leaveTimeSlot: String
  ): LeaveWorkflowConfig
}
