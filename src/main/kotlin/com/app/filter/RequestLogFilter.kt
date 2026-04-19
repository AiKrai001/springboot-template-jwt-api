package com.app.filter

import cn.hutool.core.lang.Snowflake
import com.app.logging.HttpLogSupport
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingResponseWrapper
import java.time.Duration
import java.util.Locale

@Component
class RequestLogFilter(
  private val snowflake: Snowflake
) : OncePerRequestFilter() {

  private val pathMatcher = AntPathMatcher()
  private val excludedPaths = listOf(
    "/doc.html",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/webjars/**",
    "/h2-console/**",
    "/actuator/**",
    "/favicon.ico",
    "/error"
  )
  private val staticExtensions = setOf(
    ".js", ".css", ".png", ".jpg", ".jpeg", ".gif", ".svg",
    ".ico", ".map", ".woff", ".woff2", ".ttf", ".eot", ".html"
  )

  override fun shouldNotFilter(request: HttpServletRequest): Boolean {
    val path = request.servletPath
    return isIgnoredPath(path) || isStaticResource(path)
  }

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    val startTime = System.currentTimeMillis()
    val reqId = HttpLogSupport.resolveRequestId(request) { snowflake.nextId().toString() }
    val mdcSnapshot = HttpLogSupport.bindRequestContext(request, reqId)
    response.setHeader(HttpLogSupport.REQUEST_ID_HEADER, reqId)
    val wrapper = ContentCachingResponseWrapper(response)
    logRequestStart(request)

    try {
      filterChain.doFilter(request, wrapper)
    } finally {
      logRequestEnd(request, wrapper, startTime)
      wrapper.copyBodyToResponse()
      mdcSnapshot.restore()
    }
  }

  /**
   * 判定当前请求url是否不需要日志打印
   *
   * @param url 请求路径
   * @return 是否需要忽略
   */
  private fun isIgnoredPath(url: String): Boolean {
    return excludedPaths.any { pattern ->
      pathMatcher.match(pattern, url)
    }
  }

  private fun isStaticResource(path: String): Boolean {
    val normalizedPath = path.lowercase(Locale.getDefault())
    return staticExtensions.any { normalizedPath.endsWith(it) }
  }

  /**
   * 请求结束时的日志打印，包含处理耗时以及响应结果
   *
   * @param wrapper   用于读取响应结果的包装类
   * @param startTime 起始时间
   */
  fun logRequestEnd(
    request: HttpServletRequest,
    wrapper: ContentCachingResponseWrapper,
    startTime: Long
  ) {
    val durationMs = System.currentTimeMillis() - startTime
    val status = wrapper.status
    log.atInfo()
      .addKeyValue("event.action", "request.complete")
      .addKeyValue("event.category", "web")
      .addKeyValue("event.type", "access")
      .addKeyValue("event.outcome", HttpLogSupport.resolveOutcome(status))
      .addKeyValue("http.request.method", request.method)
      .addKeyValue("url.path", request.requestURI)
      .addKeyValue("client.address", HttpLogSupport.resolveClientIp(request))
      .addKeyValue("user.id", HttpLogSupport.currentUserId())
      .addKeyValue("user_agent.original", HttpLogSupport.resolveUserAgent(request))
      .addKeyValue("http.response.status_code", status)
      .addKeyValue("http.response.body.bytes", wrapper.contentSize)
      .addKeyValue("event.duration", Duration.ofMillis(durationMs).toNanos())
      .addKeyValue("duration_ms", durationMs)
      .log("HTTP request completed")
  }

  private fun logRequestStart(request: HttpServletRequest) {
    log.atInfo()
      .addKeyValue("event.action", "request.start")
      .addKeyValue("event.category", "web")
      .addKeyValue("event.type", "access")
      .addKeyValue("http.request.method", request.method)
      .addKeyValue("url.path", request.requestURI)
      .addKeyValue("client.address", HttpLogSupport.resolveClientIp(request))
      .addKeyValue("user.id", HttpLogSupport.currentUserId())
      .addKeyValue("user_agent.original", HttpLogSupport.resolveUserAgent(request))
      .addKeyValue("request.params", HttpLogSupport.buildSafeParams(request))
      .log("HTTP request started")
  }

  companion object {
    private val log = org.slf4j.LoggerFactory.getLogger(RequestLogFilter::class.java)
  }
}
