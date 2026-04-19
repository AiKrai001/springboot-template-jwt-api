package com.app.service.impl

import com.app.config.exception.ServiceException
import com.app.data.dto.LeaveWorkflowCondition
import com.app.data.dto.LeaveWorkflowConfigCreateRequest
import com.app.data.dto.LeaveWorkflowConfigView
import com.app.data.dto.LeaveWorkflowNode
import com.app.data.model.LeaveWorkflowConfig
import com.app.data.model.LeaveWorkflowConfig as LeaveWorkflowConfigModel
import com.app.repository.LeaveWorkflowConfigRepository
import com.app.repository.RoleRepository
import com.app.service.LeaveWorkflowConfigService
import com.app.workflow.LeaveProcessDefinitionBuilder
import org.flowable.engine.RepositoryService
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Service
class LeaveWorkflowConfigServiceImpl(
  private val leaveWorkflowConfigRepository: LeaveWorkflowConfigRepository,
  private val roleRepository: RoleRepository,
  private val repositoryService: RepositoryService,
  private val processDefinitionBuilder: LeaveProcessDefinitionBuilder,
  private val sqlClient: KSqlClient,
  private val objectMapper: ObjectMapper
) : LeaveWorkflowConfigService {

  override fun create(request: LeaveWorkflowConfigCreateRequest): LeaveWorkflowConfigView {
    validateRequest(request)

    val processKey = "leave_${UUID.randomUUID().toString().replace("-", "")}"
    val bpmnXml = processDefinitionBuilder.buildXml(processKey, request.configName, request.nodes)
    val deployment = repositoryService.createDeployment()
      .name(request.configName)
      .key(processKey)
      .addBytes("$processKey.bpmn20.xml", bpmnXml)
      .deploy()
    val processDefinition = repositoryService.createProcessDefinitionQuery()
      .deploymentId(deployment.id)
      .processDefinitionKey(processKey)
      .latestVersion()
      .singleResult()
      ?: throw ServiceException("审批流部署失败")

    val now = Timestamp.from(Instant.now())
    val saved = sqlClient.saveCommand(
      LeaveWorkflowConfigModel {
        configName = request.configName.trim()
        priority = request.priority
        enabled = request.enabled
        conditionJson = objectMapper.writeValueAsString(request.condition)
        nodeJson = objectMapper.writeValueAsString(request.nodes)
        this.processKey = processKey
        processDefinitionId = processDefinition.id
        remark = request.remark?.trim()?.ifBlank { null }
        createTime = now
        updateTime = now
      }
    ) {
      setMode(SaveMode.INSERT_ONLY)
    }.execute().modifiedEntity
    return toView(saved)
  }

  override fun list(): List<LeaveWorkflowConfigView> {
    return leaveWorkflowConfigRepository.findAll()
      .sortedWith(compareByDescending<LeaveWorkflowConfig> { it.priority }.thenByDescending { it.configId })
      .map(::toView)
  }

  override fun resolveMatchedConfig(
    leaveType: String,
    durationHours: BigDecimal,
    leaveTimeSlot: String
  ): LeaveWorkflowConfig {
    return leaveWorkflowConfigRepository.findEnabledConfigs()
      .sortedWith(compareByDescending<LeaveWorkflowConfig> { it.priority }.thenByDescending { it.configId })
      .firstOrNull { config ->
        val condition = parseCondition(config.conditionJson)
        matches(condition, leaveType, durationHours, leaveTimeSlot)
      }
      ?: throw ServiceException("未匹配到符合条件的请假审批流配置")
  }

  private fun validateRequest(request: LeaveWorkflowConfigCreateRequest) {
    if (request.configName.isBlank()) {
      throw ServiceException("配置名称不能为空")
    }
    if (request.nodes.isEmpty()) {
      throw ServiceException("至少需要配置一个审批节点")
    }
    if (request.condition.minDurationHours != null &&
      request.condition.maxDurationHours != null &&
      request.condition.minDurationHours > request.condition.maxDurationHours
    ) {
      throw ServiceException("最小时长不能大于最大小时长")
    }

    request.nodes.forEachIndexed { index, node ->
      if (node.nodeName.isBlank()) {
        throw ServiceException("第 ${index + 1} 个审批节点名称不能为空")
      }
      if (node.approverRoleKeys.isEmpty()) {
        throw ServiceException("第 ${index + 1} 个审批节点至少需要一个审批角色")
      }
      node.approverRoleKeys.forEach { roleKey ->
        if (roleRepository.findByRoleKey(roleKey) == null) {
          throw ServiceException("审批角色不存在: $roleKey")
        }
      }
    }
  }

  private fun matches(
    condition: LeaveWorkflowCondition,
    leaveType: String,
    durationHours: BigDecimal,
    leaveTimeSlot: String
  ): Boolean {
    val typeMatched = condition.leaveTypes.isEmpty() || condition.leaveTypes.contains(leaveType)
    val timeSlotMatched = condition.timeSlots.isEmpty() || condition.timeSlots.contains(leaveTimeSlot)
    val minMatched = condition.minDurationHours == null || durationHours >= condition.minDurationHours
    val maxMatched = condition.maxDurationHours == null || durationHours <= condition.maxDurationHours
    return typeMatched && timeSlotMatched && minMatched && maxMatched
  }

  private fun toView(config: LeaveWorkflowConfig): LeaveWorkflowConfigView {
    return LeaveWorkflowConfigView(
      configId = config.configId,
      configName = config.configName,
      priority = config.priority,
      enabled = config.enabled,
      condition = parseCondition(config.conditionJson),
      nodes = parseNodes(config.nodeJson),
      processKey = config.processKey,
      processDefinitionId = config.processDefinitionId,
      remark = config.remark,
      createTime = config.createTime,
      updateTime = config.updateTime
    )
  }

  private fun parseCondition(json: String): LeaveWorkflowCondition {
    return objectMapper.readValue(json, LeaveWorkflowCondition::class.java)
  }

  private fun parseNodes(json: String): List<LeaveWorkflowNode> {
    return objectMapper.readValue(json, object : TypeReference<List<LeaveWorkflowNode>>() {})
  }
}
