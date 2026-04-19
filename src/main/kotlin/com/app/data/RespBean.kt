package com.app.data

import org.slf4j.MDC
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

/**
 * 响应实体类封装，Rest风格
 * @param code 状态码
 * @param data 响应数据
 * @param message 其他消息
 * @param T 响应数据类型
 */
data class RespBean<T>(
  val code: Int,
  val data: T?,
  val message: String?
) {
  companion object {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    fun <T> success(data: T?): RespBean<T> {
      return RespBean(200, data, "请求成功")
    }

    fun <T> success(): RespBean<T> {
      return success(null)
    }

    fun <T> forbidden(message: String): RespBean<T> {
      return failure(403, message)
    }

    fun <T> unauthorized(message: String): RespBean<T> {
      return failure(401, message)
    }

    fun <T> failure(message: String?): RespBean<T> {
      return RespBean(500, null, message)
    }

    fun <T> failure(code: Int, message: String?): RespBean<T> {
      return RespBean(code, null, message)
    }

    /**
     * 获取当前请求ID方便快速定位错误
     * @return ID
     */
    private fun requestId(): Long {
      val requestId = Optional.ofNullable(MDC.get("reqId")).orElse("0")
      return requestId.toLong()
    }
  }

  /**
   * 快速将当前实体转换为JSON字符串格式
   * @return JSON字符串
   */
  fun asJsonString(): String {
    return objectMapper.writeValueAsString(this)
  }
}
