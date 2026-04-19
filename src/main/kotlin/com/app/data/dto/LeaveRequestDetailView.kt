package com.app.data.dto

data class LeaveRequestDetailView(
  val request: LeaveRequestView,
  val records: List<LeaveApprovalRecordView>
)
