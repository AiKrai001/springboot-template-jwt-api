package com.app.leave

import com.app.data.dto.LeaveApplyRequest
import com.app.data.dto.LeaveApprovalActionRequest
import com.app.data.dto.LeaveWorkflowCondition
import com.app.data.dto.LeaveWorkflowConfigCreateRequest
import com.app.data.dto.LeaveWorkflowNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class LeaveFlowIntegrationTests {

  @Autowired
  lateinit var mockMvc: MockMvc

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Test
  fun leaveFlow_canCreateConfig_apply_andApproveThroughSerialAndParallelNodes() {
    val adminToken = loginAndGetToken("admin", "123456")
    val configRequest = LeaveWorkflowConfigCreateRequest(
      configName = "集成测试请假流",
      priority = 999,
      enabled = true,
      condition = LeaveWorkflowCondition(
        leaveTypes = listOf("ANNUAL_IT"),
        minDurationHours = BigDecimal("8.00"),
        maxDurationHours = BigDecimal("24.00"),
        timeSlots = listOf("SLOT_IT")
      ),
      nodes = listOf(
        LeaveWorkflowNode(
          nodeName = "直属经理审批",
          approverRoleKeys = listOf("manager")
        ),
        LeaveWorkflowNode(
          nodeName = "并行会签",
          approverRoleKeys = listOf("hr", "director"),
          parallel = true
        )
      ),
      remark = "integration test"
    )

    val createdConfig = responseData(
      mockMvc.perform(
        post("/api/leave-config/create")
          .header("Authorization", bearerToken(adminToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(configRequest))
      )
        .andExpect(status().isOk)
        .andReturn()
    )
    assertEquals("集成测试请假流", createdConfig["configName"].asText())

    val userToken = loginAndGetToken("user1", "123456")
    val appliedRequest = responseData(
      mockMvc.perform(
        post("/api/leave/apply")
          .header("Authorization", bearerToken(userToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            objectMapper.writeValueAsString(
              LeaveApplyRequest(
                leaveType = "ANNUAL_IT",
                leaveTimeSlot = "SLOT_IT",
                startTime = LocalDateTime.of(2026, 4, 20, 9, 0),
                endTime = LocalDateTime.of(2026, 4, 20, 18, 0),
                reason = "integration test leave"
              )
            )
          )
      )
        .andExpect(status().isOk)
        .andReturn()
    )

    val leaveRequestId = appliedRequest["leaveRequestId"].asLong()
    assertEquals("IN_APPROVAL", appliedRequest["status"].asText())
    assertNotNull(appliedRequest["processInstanceId"].asText())

    val managerToken = loginAndGetToken("manager1", "123456")
    val managerTaskId = findTaskId(managerToken, leaveRequestId)
    val afterManagerApprove = responseData(
      mockMvc.perform(
        post("/api/leave/approve")
          .header("Authorization", bearerToken(managerToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            objectMapper.writeValueAsString(
              LeaveApprovalActionRequest(
                taskId = managerTaskId,
                approved = true,
                comment = "manager approved"
              )
            )
          )
      )
        .andExpect(status().isOk)
        .andReturn()
    )
    assertEquals("IN_APPROVAL", afterManagerApprove["status"].asText())

    val hrToken = loginAndGetToken("hr1", "123456")
    val hrTaskId = findTaskId(hrToken, leaveRequestId)
    val afterHrApprove = responseData(
      mockMvc.perform(
        post("/api/leave/approve")
          .header("Authorization", bearerToken(hrToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            objectMapper.writeValueAsString(
              LeaveApprovalActionRequest(
                taskId = hrTaskId,
                approved = true,
                comment = "hr approved"
              )
            )
          )
      )
        .andExpect(status().isOk)
        .andReturn()
    )
    assertEquals("IN_APPROVAL", afterHrApprove["status"].asText())

    val directorToken = loginAndGetToken("director1", "123456")
    val directorTaskId = findTaskId(directorToken, leaveRequestId)
    val afterDirectorApprove = responseData(
      mockMvc.perform(
        post("/api/leave/approve")
          .header("Authorization", bearerToken(directorToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            objectMapper.writeValueAsString(
              LeaveApprovalActionRequest(
                taskId = directorTaskId,
                approved = true,
                comment = "director approved"
              )
            )
          )
      )
        .andExpect(status().isOk)
        .andReturn()
    )
    assertEquals("APPROVED", afterDirectorApprove["status"].asText())

    val detail = responseData(
      mockMvc.perform(
        post("/api/leave/detail")
          .header("Authorization", bearerToken(userToken))
          .param("leaveRequestId", leaveRequestId.toString())
      )
        .andExpect(status().isOk)
        .andReturn()
    )
    assertEquals("APPROVED", detail["request"]["status"].asText())
    assertEquals(3, detail["records"].size())
    assertFalse(detail["request"]["currentTaskName"].isTextual)
  }

  @Test
  fun leaveFlow_canRejectAtFirstApprovalNode_andPersistRejectedRecord() {
    val scenario = scenario("reject")
    val adminToken = loginAndGetToken("admin", "123456")
    createConfig(
      token = adminToken,
      request = LeaveWorkflowConfigCreateRequest(
        configName = scenario.configName,
        priority = 888,
        enabled = true,
        condition = LeaveWorkflowCondition(
          leaveTypes = listOf(scenario.leaveType),
          minDurationHours = BigDecimal("1.00"),
          maxDurationHours = BigDecimal("12.00"),
          timeSlots = listOf(scenario.leaveTimeSlot)
        ),
        nodes = listOf(
          LeaveWorkflowNode(
            nodeName = "直属经理审批",
            approverRoleKeys = listOf("manager")
          )
        ),
        remark = "reject test"
      )
    )

    val userToken = loginAndGetToken("user1", "123456")
    val appliedRequest = applyLeave(
      token = userToken,
      request = LeaveApplyRequest(
        leaveType = scenario.leaveType,
        leaveTimeSlot = scenario.leaveTimeSlot,
        startTime = LocalDateTime.of(2026, 4, 21, 9, 0),
        endTime = LocalDateTime.of(2026, 4, 21, 15, 0),
        reason = "reject scenario"
      )
    )
    val leaveRequestId = appliedRequest["leaveRequestId"].asLong()

    val managerToken = loginAndGetToken("manager1", "123456")
    val managerTaskId = findTaskId(managerToken, leaveRequestId)
    val rejectedRequest = responseData(
      mockMvc.perform(
        post("/api/leave/approve")
          .header("Authorization", bearerToken(managerToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            objectMapper.writeValueAsString(
              LeaveApprovalActionRequest(
                taskId = managerTaskId,
                approved = false,
                comment = "manager rejected"
              )
            )
          )
      )
        .andExpect(status().isOk)
        .andReturn()
    )

    assertEquals("REJECTED", rejectedRequest["status"].asText())
    assertTrue(rejectedRequest["currentTaskName"].isNull)

    val detail = responseData(
      mockMvc.perform(
        post("/api/leave/detail")
          .header("Authorization", bearerToken(userToken))
          .param("leaveRequestId", leaveRequestId.toString())
      )
        .andExpect(status().isOk)
        .andReturn()
    )
    assertEquals("REJECTED", detail["request"]["status"].asText())
    assertEquals(1, detail["records"].size())
    assertEquals("REJECTED", detail["records"][0]["action"].asText())
    assertFalse(hasTodoTask(managerToken, leaveRequestId))
  }

  @Test
  fun leaveConfig_create_should_forbid_non_admin_user() {
    val scenario = scenario("forbid")
    val userToken = loginAndGetToken("user1", "123456")

    val root = responseRoot(
      mockMvc.perform(
        post("/api/leave-config/create")
          .header("Authorization", bearerToken(userToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            objectMapper.writeValueAsString(
              LeaveWorkflowConfigCreateRequest(
                configName = scenario.configName,
                priority = 100,
                enabled = true,
                condition = LeaveWorkflowCondition(
                  leaveTypes = listOf(scenario.leaveType),
                  timeSlots = listOf(scenario.leaveTimeSlot)
                ),
                nodes = listOf(
                  LeaveWorkflowNode(
                    nodeName = "直属经理审批",
                    approverRoleKeys = listOf("manager")
                  )
                )
              )
            )
          )
      )
        .andExpect(status().isForbidden)
        .andReturn()
    )

    assertEquals(403, root["code"].asInt())
    assertTrue(root["message"].asText().contains("没有权限"))
  }

  @Test
  fun leaveFlow_apply_should_return_error_when_no_config_matches() {
    val scenario = scenario("no-match")
    val userToken = loginAndGetToken("user1", "123456")

    val root = responseRoot(
      mockMvc.perform(
        post("/api/leave/apply")
          .header("Authorization", bearerToken(userToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            objectMapper.writeValueAsString(
              LeaveApplyRequest(
                leaveType = scenario.leaveType,
                leaveTimeSlot = scenario.leaveTimeSlot,
                startTime = LocalDateTime.of(2026, 4, 22, 9, 0),
                endTime = LocalDateTime.of(2026, 4, 22, 12, 0),
                reason = "no matched config"
              )
            )
          )
      )
        .andExpect(status().isInternalServerError)
        .andReturn()
    )

    assertEquals(500, root["code"].asInt())
    assertTrue(root["message"].asText().contains("未匹配到符合条件的请假审批流配置"))
  }

  private fun findTaskId(token: String, leaveRequestId: Long): String {
    val tasks = responseData(
      mockMvc.perform(
        post("/api/leave/todo-tasks")
          .header("Authorization", bearerToken(token))
      )
        .andExpect(status().isOk)
        .andReturn()
    )
    return tasks.first { it["leaveRequestId"].asLong() == leaveRequestId }["taskId"].asText()
  }

  private fun hasTodoTask(token: String, leaveRequestId: Long): Boolean {
    val tasks = responseData(
      mockMvc.perform(
        post("/api/leave/todo-tasks")
          .header("Authorization", bearerToken(token))
      )
        .andExpect(status().isOk)
        .andReturn()
    )
    return tasks.any { it["leaveRequestId"].asLong() == leaveRequestId }
  }

  private fun createConfig(
    token: String,
    request: LeaveWorkflowConfigCreateRequest
  ): JsonNode {
    return responseData(
      mockMvc.perform(
        post("/api/leave-config/create")
          .header("Authorization", bearerToken(token))
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
      )
        .andExpect(status().isOk)
        .andReturn()
    )
  }

  private fun applyLeave(
    token: String,
    request: LeaveApplyRequest
  ): JsonNode {
    return responseData(
      mockMvc.perform(
        post("/api/leave/apply")
          .header("Authorization", bearerToken(token))
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
      )
        .andExpect(status().isOk)
        .andReturn()
    )
  }

  private fun loginAndGetToken(username: String, password: String): String {
    val responseBody = mockMvc.perform(
      post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          objectMapper.writeValueAsString(
            mapOf("username" to username, "password" to password)
          )
        )
    )
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString
      .trim()

    if (!responseBody.startsWith("{")) {
      return responseBody
    }
    val root = objectMapper.readTree(responseBody)
    return root["data"].asText()
  }

  private fun responseData(result: org.springframework.test.web.servlet.MvcResult): JsonNode {
    return responseRoot(result)["data"]
  }

  private fun responseRoot(result: org.springframework.test.web.servlet.MvcResult): JsonNode {
    return objectMapper.readTree(result.response.contentAsString)
  }

  private fun scenario(prefix: String): LeaveScenario {
    val suffix = UUID.randomUUID().toString().replace("-", "").takeLast(8)
    return LeaveScenario(
      configName = "请假流-$prefix-$suffix",
      leaveType = "TYPE_$suffix",
      leaveTimeSlot = "SLOT_$suffix"
    )
  }

  private fun bearerToken(token: String): String = "Bearer $token"

  private data class LeaveScenario(
    val configName: String,
    val leaveType: String,
    val leaveTimeSlot: String
  )
}
