package com.app.config

import com.app.config.satoken.SaTokenConfig
import com.app.data.RespBean
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import tools.jackson.databind.ObjectMapper

@RestControllerAdvice
class ResponseAdvisor(
  private val objectMapper: ObjectMapper
) : ResponseBodyAdvice<Any> {
  private val pathMatcher = AntPathMatcher()
  override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
    // Do not apply when Spring chose ByteArray converter
    val controllerClassName = try {
      (returnType.containingClass.name ?: returnType.parameterType.name) ?: ""
    } catch (e: Exception) {
      ""
    }

    if (controllerClassName.startsWith("org.springdoc")) return false

    return try {
      val byteArrayConv = Class.forName("org.springframework.http.converter.ByteArrayHttpMessageConverter")
      !byteArrayConv.isAssignableFrom(converterType)
    } catch (e: ClassNotFoundException) {
      true
    }
  }

  override fun beforeBodyWrite(
    body: Any?,
    returnType: MethodParameter,
    selectedContentType: MediaType,
    selectedConverterType: Class<out HttpMessageConverter<*>>,
    request: ServerHttpRequest,
    response: ServerHttpResponse
  ): Any? {
    val path = request.uri.path
    if (SaTokenConfig.excludePath.any { pattern -> pathMatcher.match(pattern, path) }) return body

    // When Spring chooses ByteArrayHttpMessageConverter, keep original body
    if (ByteArray::class.java.isAssignableFrom(returnType.parameterType) ||
      selectedConverterType.name.contains("ByteArrayHttpMessageConverter") ||
      selectedContentType == MediaType.APPLICATION_OCTET_STREAM
    ) {
      return body
    }
    if (body == null) {
      return RespBean.success<Any>(null)
    }

    if (body is String) {
      response.headers.contentType = MediaType.APPLICATION_JSON
      return objectMapper.writeValueAsString(RespBean.success(body))
    }

    if (body is RespBean<*>) return body

    if (request.uri.path == "/error") {
      val mapBody = body as Map<*, *>
      return RespBean.failure<String>(mapBody["status"].toString().toInt(), mapBody["error"].toString())
    }

    return RespBean.success(body)
  }
}
