package com.app.repository

import com.app.data.model.LeaveRequest
import com.app.data.model.applicantId
import com.app.data.model.leaveRequestId
import com.app.data.model.processInstanceId
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq

interface LeaveRequestRepository : KRepository<LeaveRequest, Long> {
  fun findByLeaveRequestId(id: Long): LeaveRequest? =
    sql.createQuery(LeaveRequest::class) {
      where(table.leaveRequestId eq id)
      select(table)
    }.execute().firstOrNull()

  fun findByApplicantIdValue(userId: Long): List<LeaveRequest> =
    sql.createQuery(LeaveRequest::class) {
      where(table.applicantId eq userId)
      select(table)
    }.execute()

  fun findByProcessInstanceIdValue(instanceId: String): LeaveRequest? =
    sql.createQuery(LeaveRequest::class) {
      where(table.processInstanceId eq instanceId)
      select(table)
    }.execute().firstOrNull()
}
