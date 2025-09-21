package com.app.rbac

import com.app.data.dto.AssignPermissionsRequest
import com.app.data.dto.AssignRolesRequest
import com.app.service.PermissionService
import com.app.service.RoleService
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class RbacIntegrationTests {

  @Autowired
  lateinit var mockMvc: MockMvc

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Autowired
  lateinit var roleService: RoleService

  @Autowired
  lateinit var permissionService: PermissionService

  private fun loginAndGetToken(username: String, password: String): String {
    val body = objectMapper.writeValueAsString(mapOf("username" to username, "password" to password))
    val result = mockMvc.perform(
      post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
    )
      .andExpect(status().isOk)
      .andReturn()
    val token = result.response.contentAsString.trim()
    assertTrue(token.isNotBlank(), "Token should not be blank")
    return token
  }

  @Test
  fun login_success_returnsToken() {
    loginAndGetToken("admin", "123456")
  }

  @Test
  fun role_and_permission_list_require_login_and_return_data() {
    val token = loginAndGetToken("admin", "123456")

    // role list
    mockMvc.perform(
      post("/api/role/list")
        .header("satoken", token)
        .param("pageNum", "0")
        .param("pageSize", "10")
    )
      .andExpect(status().isOk)
      .andExpect(content().string(not(containsString("Unauthorized"))))

    // permission list
    mockMvc.perform(
      post("/api/permission/list")
        .header("satoken", token)
        .param("pageNum", "0")
        .param("pageSize", "10")
    )
      .andExpect(status().isOk)
      .andExpect(content().string(not(containsString("Unauthorized"))))
  }

  @Test
  fun assign_role_to_user_and_perm_to_role_then_verify() {
    val token = loginAndGetToken("admin", "123456")

    // Assign admin role (id=1) to user1 (id=2)
    val assignRoleReq = AssignRolesRequest(userId = 2L, roleIds = listOf(1L))
    mockMvc.perform(
      post("/api/role/assign-to-user")
        .header("satoken", token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(assignRoleReq))
    )
      .andExpect(status().isOk)

    val roleKeys = roleService.findRoleKeysByUserId(2L)
    assertTrue(roleKeys.contains("admin"), "user1 should have admin role after assignment")

    // Assign 'user:create' (permission id=2) to user role (id=2)
    val assignPermReq = AssignPermissionsRequest(roleId = 2L, permissionIds = listOf(2L))
    mockMvc.perform(
      post("/api/permission/assign-to-role")
        .header("satoken", token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(assignPermReq))
    )
      .andExpect(status().isOk)

    val permKeys = permissionService.findPermissionKeysByUserId(2L)
    assertTrue(permKeys.contains("user:create"), "user1 should get 'user:create' via role after assignment")
  }
}
