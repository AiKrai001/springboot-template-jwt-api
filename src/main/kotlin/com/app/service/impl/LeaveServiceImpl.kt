package com.app.service.impl

import cn.dev33.satoken.stp.StpUtil
import com.app.config.exception.ServiceException
import com.app.data.dto.LeaveApprovalActionRequest
import com.app.data.dto.LeaveApprovalRecordView
import com.app.data.dto.LeaveRequestDetailView
import com.app.data.dto.LeaveRequestView
import com.app.data.dto.LeaveApplyRequest
import com.app.data.dto.LeaveTodoTaskView
import com.app.data.enums.LeaveApprovalAction
import com.app.data.enums.LeaveRequestStatus
import com.app.data.model.LeaveApprovalRecord
import com.app.data.model.LeaveApprovalRecord as LeaveApprovalRecordModel
import com.app.data.model.LeaveRequest
import com.app.data.model.LeaveRequest as LeaveRequestModel
import com.app.repository.LeaveApprovalRecordRepository
import com.app.repository.LeaveRequestRepository
import com.app.repository.UserRepository
import com.app.service.LeaveService
import com.app.service.LeaveWorkflowConfigService
import com.app.service.RoleService
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.flowable.engine.RuntimeService
import org.flowable.engine.TaskService
import org.flowable.task.api.Task
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant

@Service
class LeaveServiceImpl(
  private val leaveRequestRepository: LeaveRequestRepository,
  private val leaveApprovalRecordRepository: LeaveApprovalRecordRepository,
  private val leaveWorkflowConfigService: LeaveWorkflowConfigService,
  private val userRepository: UserRepository,
  private val roleService: RoleService,
  private val sqlClient: KSqlClient,
  private val runtimeService: RuntimeService,
  private val taskService: TaskService
) : LeaveService {
  override fun apply(request: LeaveApplyRequest): LeaveRequestView {
    val applicantId = currentUserId()
    val applicant = userRepository.findByUserId(applicantId) ?: throw ServiceException("当前登录用户不存在")
    val durationHours = calculateDurationHours(request)
    val matchedConfig = leaveWorkflowConfigService.resolveMatchedConfig(
      leaveType = request.leaveType,
      durationHours = durationHours,
      leaveTimeSlot = request.leaveTimeSlot
    )
    val now = Timestamp.from(Instant.now())

    val savedRequest = sqlClient.saveCommand(
      LeaveRequestModel {
        this.applicantId = applicantId
        leaveType = request.leaveType.trim()
        leaveTimeSlot = request.leaveTimeSlot.trim()
        startTime = Timestamp.valueOf(request.startTime)
        endTime = Timestamp.valueOf(request.endTime)
        this.durationHours = durationHours
        reason = request.reason?.trim()?.ifBlank { null }
        status = LeaveRequestStatus.IN_APPROVAL.name
        configId = matchedConfig.configId
        createTime = now
        updateTime = now
      }
    ) {
      setMode(SaveMode.INSERT_ONLY)
    }.execute().modifiedEntity

    val processInstance = runtimeService.startProcessInstanceByKey(
      matchedConfig.processKey,
      savedRequest.leaveRequestId.toString(),
      mapOf(
        "leaveRequestId" to savedRequest.leaveRequestId,
        "applicantId" to applicantId,
        "applicantName" to (applicant.nickName ?: applicant.userName),
        "leaveType" to savedRequest.leaveType,
        "leaveTimeSlot" to savedRequest.leaveTimeSlot,
        "durationHours" to savedRequest.durationHours
      )
    )
    val currentTaskName = resolveCurrentTaskName(processInstance.id)
    val updatedRequest = leaveRequestRepository.save(
      rebuildLeaveRequest(
        base = savedRequest,
        processDefinitionId = processInstance.processDefinitionId,
        processInstanceId = processInstance.id,
        currentTaskName = currentTaskName,
        updateTime = Timestamp.from(Instant.now())
      )
    )
    return toView(updatedRequest)
  }

  override fun myRequests(): List<LeaveRequestView> {
    return leaveRequestRepository.findByApplicantIdValue(currentUserId())
      .sortedByDescending { it.leaveRequestId }
      .map(::toView)
  }

  override fun todoTasks(): List<LeaveTodoTaskView> {
    val roleKeys = roleService.findRoleKeysByUserId(currentUserId())
    if (roleKeys.isEmpty()) {
      return emptyList()
    }
    return taskService.createTaskQuery()
      .taskCandidateGroupIn(roleKeys)
      .active()
      .list()
      .mapNotNull { task ->
        val leaveRequest = leaveRequestRepository.findByProcessInstanceIdValue(task.processInstanceId) ?: return@mapNotNull null
        val applicant = userRepository.findByUserId(leaveRequest.applicantId)
        LeaveTodoTaskView(
          taskId = task.id,
          taskName = task.name,
          processInstanceId = task.processInstanceId,
          leaveRequestId = leaveRequest.leaveRequestId,
          applicantId = leaveRequest.applicantId,
          applicantName = applicant?.nickName ?: applicant?.userName,
          leaveType = leaveRequest.leaveType,
          leaveTimeSlot = leaveRequest.leaveTimeSlot,
          durationHours = leaveRequest.durationHours,
          reason = leaveRequest.reason,
          createdTime = task.createTime
        )
      }
  }

  override fun approve(request: LeaveApprovalActionRequest): LeaveRequestView {
    val task = taskService.createTaskQuery().taskId(request.taskId).singleResult()
      ?: throw ServiceException("审批任务不存在或已处理")
    ensureCurrentUserCanApprove(task)

    val leaveRequest = leaveRequestRepository.findByProcessInstanceIdValue(task.processInstanceId)
      ?: throw ServiceException("未找到对应的请假单")
    val approver = userRepository.findByUserId(currentUserId()) ?: throw ServiceException("审批用户不存在")

    if (request.comment?.isNotBlank() == true) {
      taskService.addComment(task.id, task.processInstanceId, request.comment.trim())
    }
    saveApprovalRecord(
      leaveRequestId = leaveRequest.leaveRequestId,
      task = task,
      action = if (request.approved) LeaveApprovalAction.APPROVED else LeaveApprovalAction.REJECTED,
      approverId = approver.userId,
      approverName = approver.nickName ?: approver.userName,
      comment = request.comment
    )

    return if (request.approved) {
      taskService.complete(task.id)
      refreshLeaveRequestAfterTask(leaveRequest)
    } else {
      runtimeService.deleteProcessInstance(task.processInstanceId, "Rejected by ${approver.userName}")
      updateLeaveRequestStatus(
        leaveRequest = leaveRequest,
        status = LeaveRequestStatus.REJECTED,
        currentTaskName = null
      )
    }
  }

  override fun detail(leaveRequestId: Long): LeaveRequestDetailView {
    val leaveRequest = leaveRequestRepository.findByLeaveRequestId(leaveRequestId)
      ?: throw ServiceException("请假单不存在")
    val records = leaveApprovalRecordRepository.findByLeaveRequestIdValue(leaveRequestId)
      .sortedBy { it.recordId }
      .map(::toRecordView)
    return LeaveRequestDetailView(
      request = toView(leaveRequest),
      records = records
    )
  }

  private fun calculateDurationHours(request: LeaveApplyRequest): BigDecimal {
    if (request.leaveType.isBlank()) {
      throw ServiceException("请假类型不能为空")
    }
    if (request.leaveTimeSlot.isBlank()) {
      throw ServiceException("请假时间节点不能为空")
    }
    if (!request.startTime.isBefore(request.endTime)) {
      throw ServiceException("请假结束时间必须大于开始时间")
    }
    val minutes = Duration.between(request.startTime, request.endTime).toMinutes()
    return BigDecimal.valueOf(minutes)
      .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
  }

  private fun ensureCurrentUserCanApprove(task: Task) {
    val roleKeys = roleService.findRoleKeysByUserId(currentUserId())
    val matched = taskService.getIdentityLinksForTask(task.id)
      .filter { it.groupId != null }
      .mapNotNull { it.groupId }
      .any(roleKeys::contains)
    if (!matched) {
      throw ServiceException("当前用户无权审批该任务")
    }
  }

  private fun saveApprovalRecord(
    leaveRequestId: Long,
    task: Task,
    action: LeaveApprovalAction,
    approverId: Long,
    approverName: String,
    comment: String?
  ) {
    sqlClient.saveCommand(
      LeaveApprovalRecordModel {
        this.leaveRequestId = leaveRequestId
        taskId = task.id
        taskName = task.name
        this.action = action.name
        this.approverId = approverId
        this.approverName = approverName
        this.comment = comment?.trim()?.ifBlank { null }
        createTime = Timestamp.from(Instant.now())
      }
    ) {
      setMode(SaveMode.INSERT_ONLY)
    }.execute()
  }

  private fun refreshLeaveRequestAfterTask(leaveRequest: LeaveRequest): LeaveRequestView {
    val stillRunning = runtimeService.createProcessInstanceQuery()
      .processInstanceId(leaveRequest.processInstanceId)
      .singleResult()

    return if (stillRunning == null) {
      toView(
        leaveRequestRepository.save(
          rebuildLeaveRequest(
            base = leaveRequest,
            status = LeaveRequestStatus.APPROVED.name,
            currentTaskName = null,
            updateTime = Timestamp.from(Instant.now())
          )
        )
      )
    } else {
      toView(
        leaveRequestRepository.save(
          rebuildLeaveRequest(
            base = leaveRequest,
            currentTaskName = resolveCurrentTaskName(leaveRequest.processInstanceId!!),
            updateTime = Timestamp.from(Instant.now())
          )
        )
      )
    }
  }

  private fun updateLeaveRequestStatus(
    leaveRequest: LeaveRequest,
    status: LeaveRequestStatus,
    currentTaskName: String?
  ): LeaveRequestView {
    return toView(
      leaveRequestRepository.save(
        rebuildLeaveRequest(
          base = leaveRequest,
          status = status.name,
          currentTaskName = currentTaskName,
          updateTime = Timestamp.from(Instant.now())
        )
      )
    )
  }

  private fun resolveCurrentTaskName(processInstanceId: String): String? {
    return taskService.createTaskQuery()
      .processInstanceId(processInstanceId)
      .active()
      .list()
      .map { it.name }
      .distinct()
      .ifEmpty { null }
      ?.joinToString(" / ")
  }

  private fun toView(leaveRequest: LeaveRequest): LeaveRequestView {
    val applicant = userRepository.findByUserId(leaveRequest.applicantId)
    return LeaveRequestView(
      leaveRequestId = leaveRequest.leaveRequestId,
      applicantId = leaveRequest.applicantId,
      applicantName = applicant?.nickName ?: applicant?.userName,
      leaveType = leaveRequest.leaveType,
      leaveTimeSlot = leaveRequest.leaveTimeSlot,
      startTime = leaveRequest.startTime,
      endTime = leaveRequest.endTime,
      durationHours = leaveRequest.durationHours,
      reason = leaveRequest.reason,
      status = leaveRequest.status,
      configId = leaveRequest.configId,
      processDefinitionId = leaveRequest.processDefinitionId,
      processInstanceId = leaveRequest.processInstanceId,
      currentTaskName = leaveRequest.currentTaskName,
      createTime = leaveRequest.createTime,
      updateTime = leaveRequest.updateTime
    )
  }

  private fun toRecordView(record: LeaveApprovalRecord): LeaveApprovalRecordView {
    return LeaveApprovalRecordView(
      recordId = record.recordId,
      taskId = record.taskId,
      taskName = record.taskName,
      action = record.action,
      approverId = record.approverId,
      approverName = record.approverName,
      comment = record.comment,
      createTime = record.createTime
    )
  }

  private fun currentUserId(): Long {
    return StpUtil.getLoginId().toString().toLong()
  }

  private fun rebuildLeaveRequest(
    base: LeaveRequest,
    status: String = base.status,
    processDefinitionId: String? = base.processDefinitionId,
    processInstanceId: String? = base.processInstanceId,
    currentTaskName: String? = base.currentTaskName,
    updateTime: Timestamp = base.updateTime ?: Timestamp.from(Instant.now())
  ): LeaveRequest {
    return LeaveRequestModel {
      leaveRequestId = base.leaveRequestId
      applicantId = base.applicantId
      leaveType = base.leaveType
      leaveTimeSlot = base.leaveTimeSlot
      startTime = base.startTime
      endTime = base.endTime
      durationHours = base.durationHours
      reason = base.reason
      this.status = status
      configId = base.configId
      this.processDefinitionId = processDefinitionId
      this.processInstanceId = processInstanceId
      this.currentTaskName = currentTaskName
      createTime = base.createTime
      this.updateTime = updateTime
    }
  }
}
