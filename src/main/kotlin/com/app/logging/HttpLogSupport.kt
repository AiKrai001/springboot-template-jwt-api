package com.app.logging

import cn.dev33.satoken.stp.StpUtil
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.MDC

object HttpLogSupport {
  const val REQUEST_ID_HEADER = "X-Request-Id"

  private const val REQUEST_ID_KEY = "reqId"
  private const val CLIENT_IP_KEY = "clientIp"
  private const val HTTP_METHOD_KEY = "httpMethod"
  private const val HTTP_PATH_KEY = "httpPath"

  private val managedKeys = listOf(
    REQUEST_ID_KEY,
    CLIENT_IP_KEY,
    HTTP_METHOD_KEY,
    HTTP_PATH_KEY
  )

  private val sensitiveKeys = setOf("password", "token", "authorization", "captcha", "secret", "refreshToken")
  private val requestIdPattern = Regex("^[A-Za-z0-9._-]{1,64}$")

  fun resolveRequestId(request: HttpServletRequest, generator: () -> String): String {
    val headerValue = request.getHeader(REQUEST_ID_HEADER)
      ?.trim()
      ?.take(64)
      ?.takeIf { it.isNotBlank() && requestIdPattern.matches(it) }

    return headerValue ?: generator()
  }

  fun bindRequestContext(request: HttpServletRequest, requestId: String): MdcSnapshot {
    val previousValues = managedKeys.associateWith { key -> MDC.get(key) }

    MDC.put(REQUEST_ID_KEY, requestId)
    MDC.put(CLIENT_IP_KEY, resolveClientIp(request))
    MDC.put(HTTP_METHOD_KEY, request.method)
    MDC.put(HTTP_PATH_KEY, request.requestURI ?: request.servletPath)

    return MdcSnapshot(previousValues)
  }

  fun buildSafeParams(request: HttpServletRequest): Map<String, String> {
    return request.parameterMap.mapValues { (key, values) ->
      when {
        sensitiveKeys.any { it.equals(key, ignoreCase = true) } -> "***"
        values.isEmpty() -> ""
        else -> values.first().take(128)
      }
    }
  }

  fun resolveClientIp(request: HttpServletRequest): String {
    val forwardedFor = request.getHeader("X-Forwarded-For")
    if (!forwardedFor.isNullOrBlank()) {
      return forwardedFor.substringBefore(',').trim()
    }

    val realIp = request.getHeader("X-Real-IP")
    if (!realIp.isNullOrBlank()) {
      return realIp
    }

    return request.remoteAddr ?: "unknown"
  }

  fun currentUserId(): String {
    return runCatching {
      if (StpUtil.isLogin()) StpUtil.getLoginId().toString() else "anonymous"
    }.getOrDefault("anonymous")
  }

  fun resolveOutcome(status: Int): String {
    return when {
      status >= 500 -> "failure"
      status >= 400 -> "client_error"
      else -> "success"
    }
  }

  fun resolveUserAgent(request: HttpServletRequest): String {
    return request.getHeader("User-Agent")
      ?.replace(Regex("[\\r\\n]+"), " ")
      ?.trim()
      ?.take(256)
      ?.ifBlank { null }
      ?: "unknown"
  }

  data class MdcSnapshot(
    private val previousValues: Map<String, String?>
  ) {
    fun restore() {
      previousValues.forEach { (key, value) ->
        if (value == null) {
          MDC.remove(key)
        } else {
          MDC.put(key, value)
        }
      }
    }
  }
}
