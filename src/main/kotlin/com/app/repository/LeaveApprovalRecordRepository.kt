package com.app.repository

import com.app.data.model.LeaveApprovalRecord
import com.app.data.model.leaveRequestId
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq

interface LeaveApprovalRecordRepository : KRepository<LeaveApprovalRecord, Long> {
  fun findByLeaveRequestIdValue(requestId: Long): List<LeaveApprovalRecord> =
    sql.createQuery(LeaveApprovalRecord::class) {
      where(table.leaveRequestId eq requestId)
      select(table)
    }.execute()
}
