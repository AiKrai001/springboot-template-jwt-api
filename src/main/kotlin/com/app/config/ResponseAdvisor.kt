package com.app.config

import com.app.data.RespBean
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@ControllerAdvice
class ResponseAdvisor(
  private val objectMapper: ObjectMapper
) : ResponseBodyAdvice<Any> {
  override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
    return true
  }

  override fun beforeBodyWrite(
    body: Any?,
    returnType: MethodParameter,
    selectedContentType: MediaType,
    selectedConverterType: Class<out HttpMessageConverter<*>>,
    request: ServerHttpRequest,
    response: ServerHttpResponse
  ): Any? {
    if (body == null) {
      return RespBean.success<Any>(null)
    }

    if (body is String) {
      response.headers.contentType = MediaType.APPLICATION_JSON
      return objectMapper.writeValueAsString(RespBean.success(body))
    }

    if (body is RespBean<*>) return body

    if (request.uri.path == "/error") {
      val body = body as Map<*, *>
      return RespBean.failure<String>(body["status"].toString().toInt(), body["error"].toString())
    }

    return RespBean.success(body)
  }
}
