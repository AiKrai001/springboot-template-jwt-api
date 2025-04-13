package com.app.filter

import cn.dev33.satoken.stp.StpUtil
import cn.hutool.core.lang.Snowflake
import cn.hutool.json.JSONObject
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingResponseWrapper

@Slf4j
@Component
class RequestLogFilter(
  private val snowflake: Snowflake
) : OncePerRequestFilter() {

  private val ignores = setOf("/chatjava", "/ai", "/doc.html", "/swagger-ui", "/v3/api-docs")

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    if (isIgnoreUrl(request.servletPath)) {
      filterChain.doFilter(request, response)
    } else {
      val startTime = System.currentTimeMillis()
      logRequestStart(request)
      val wrapper = ContentCachingResponseWrapper(response)
      filterChain.doFilter(request, wrapper)
      logRequestEnd(wrapper, startTime)
      wrapper.copyBodyToResponse()
    }
  }

  /**
   * 判定当前请求url是否不需要日志打印
   *
   * @param url 路径
   * @return 是否忽略
   */
  private fun isIgnoreUrl(url: String): Boolean {
    return ignores.any { url.startsWith(it) }
  }

  /**
   * 请求结束时的日志打印，包含处理耗时以及响应结果
   *
   * @param wrapper   用于读取响应结果的包装类
   * @param startTime 起始时间
   */
  fun logRequestEnd(wrapper: ContentCachingResponseWrapper, startTime: Long) {
    val time = System.currentTimeMillis() - startTime
    val status = wrapper.status
    val content = if (status != 200) "$status 错误"
    else String(wrapper.contentAsByteArray)

    log.info("\n>>>>>请求处理耗时:[{}ms] 响应结果:{}", time, content)
  }

  /**
   * 请求开始时的日志打印，包含请求全部信息，以及对应用户角色
   *
   * @param request 请求
   */
  fun logRequestStart(request: HttpServletRequest) {
    val reqId = snowflake.nextId()
    MDC.put("reqId", reqId.toString())

    val params = JSONObject().apply {
      request.parameterMap.forEach { (k, v) ->
        put(k, if (v.isNotEmpty()) v[0] else null)
      }
    }

    if (StpUtil.isLogin()) {
      val id = StpUtil.getLoginId()
      log.info("""
        
                >>>>>请求ID:[${reqId}]
                >>>>>请求URL:["${request.servletPath}"](${request.method}) 
                >>>>>远程IP:[${request.remoteAddr}] 
                >>>>>用户名:[username] 
                >>>>>用户ID:$id 
                >>>>>角色:${StpUtil.getRoleList()} 
                >>>>>请求参数列表: [$params]
                """.trimIndent())
    } else {
      log.info("""
        
                >>>>>请求ID:[${reqId}]
                >>>>>请求URL:["${request.servletPath}"](${request.method}) 
                >>>>>远程IP地址:[${request.remoteAddr}] 
                >>>>>身份:未验证 
                >>>>>请求参数列表: [$params]
                """.trimIndent())
    }
  }

  companion object {
    private val log = org.slf4j.LoggerFactory.getLogger(RequestLogFilter::class.java)
  }
}
