package com.app.filter

import com.app.data.RespBean
import com.app.utlis.Const
import com.app.utlis.FlowUtils
import jakarta.annotation.Resource
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.PrintWriter

/**
 * 限流控制过滤器
 * 防止用户高频请求接口，借助Redis进行限流
 */
@Slf4j
@Component
@Order(Const.ORDER_FLOW_LIMIT)
class FlowLimitingFilter : HttpFilter() {

  @Resource
  private lateinit var template: StringRedisTemplate

  // 指定时间内最大请求次数限制
  @Value("\${spring.web.flow.limit}")
  private var limit: Int = 0

  // 计数时间周期
  @Value("\${spring.web.flow.period}")
  private var period: Int = 0

  // 超出请求限制封禁时间
  @Value("\${spring.web.flow.block}")
  private var block: Int = 0

  @Resource
  private lateinit var utils: FlowUtils

  @Throws(IOException::class, ServletException::class)
  override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    val address = request.remoteAddr
    if ("OPTIONS" != request.method && !tryCount(address)) {
      writeBlockMessage(response)
    } else {
      chain.doFilter(request, response)
    }
  }

  /**
   * 尝试对指定IP地址请求计数，如果被限制则无法继续访问
   * @param address 请求IP地址
   * @return 是否操作成功
   */
  private fun tryCount(address: String): Boolean {
    synchronized(address.intern()) {
      if (template.hasKey(Const.FLOW_LIMIT_BLOCK + address) == true) {
        return false
      }
      val counterKey = Const.FLOW_LIMIT_COUNTER + address
      val blockKey = Const.FLOW_LIMIT_BLOCK + address
      return utils.limitPeriodCheck(counterKey, blockKey, block, limit, period)
    }
  }

  /**
   * 为响应编写拦截内容，提示用户操作频繁
   * @param response 响应
   * @throws IOException 可能的异常
   */
  @Throws(IOException::class)
  private fun writeBlockMessage(response: HttpServletResponse) {
    response.status = 429
    response.contentType = "application/json;charset=utf-8"
    val writer: PrintWriter = response.writer
    writer.write(RespBean.failure<Any>(429, "请求频率过快，请稍后再试").asJsonString())
  }

  companion object {
    private val log = org.slf4j.LoggerFactory.getLogger(FlowLimitingFilter::class.java)
  }
}
