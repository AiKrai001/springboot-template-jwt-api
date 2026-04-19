package com.app

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class WebRequestBoundaryTests {

  @Autowired
  lateinit var mockMvc: MockMvc

  @Test
  fun missing_non_api_resource_should_return_404_instead_of_401() {
    mockMvc.perform(get("/favicsssson.ico"))
      .andExpect(status().isNotFound)
      .andExpect(content().string(containsString("\"code\":404")))
      .andExpect(content().string(not(containsString("\"code\":401"))))
  }

  @Test
  fun protected_api_still_requires_login() {
    mockMvc.perform(post("/api/auth/logout"))
      .andExpect(status().isUnauthorized)
      .andExpect(content().string(containsString("\"code\":401")))
  }

  @Test
  fun request_id_header_should_be_echoed_for_api_requests() {
    mockMvc.perform(
      post("/api/auth/logout")
        .header("X-Request-Id", "req-test-123")
    )
      .andExpect(status().isUnauthorized)
      .andExpect(header().string("X-Request-Id", "req-test-123"))
      .andExpect(content().string(containsString("\"code\":401")))
  }

  @Test
  fun jimmer_page_response_should_be_serializable() {
    mockMvc.perform(
      post("/api/user/list")
        .param("pageNum", "0")
        .param("pageSize", "10")
    )
      .andExpect(status().isOk)
      .andExpect(content().string(containsString("\"code\":200")))
      .andExpect(content().string(containsString("\"rows\"")))
      .andExpect(content().string(not(containsString("Immutable object cannot be serialized"))))
  }
}
