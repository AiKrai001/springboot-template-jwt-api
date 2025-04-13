package com.example.controller.exception

import com.app.data.RespBean
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * 专用用于处理错误页面的Controller
 */
@RestController
@RequestMapping("\${server.error.path:\${error.path:/error}}")
class ErrorPageController(errorAttributes: ErrorAttributes) : AbstractErrorController(errorAttributes) {

  /**
   * 所有错误在这里统一处理，自动解析状态码和原因
   * @param request 请求
   * @return 失败响应
   */
  @RequestMapping
  fun error(request: HttpServletRequest): RespBean<Void> {
    val status = getStatus(request)
    val errorAttributes = getErrorAttributes(request, attributeOptions)
    val message = convertErrorMessage(status)
      .orElse(errorAttributes["message"].toString())
    return RespBean.failure(status.value(), message)
  }

  /**
   * 对于一些特殊的状态码，错误信息转换
   * @param status 状态码
   * @return 错误信息
   */
  private fun convertErrorMessage(status: HttpStatus): Optional<String> {
    val value = when (status.value()) {
      400 -> "请求参数有误"
      404 -> "请求的接口不存在"
      405 -> "请求方法错误"
      500 -> "内部错误，请联系管理员"
      else -> null
    }
    return Optional.ofNullable(value)
  }

  /**
   * 错误属性获取选项，这里额外添加了错误消息和异常类型
   * @return 选项
   */
  private val attributeOptions: ErrorAttributeOptions
    get() = ErrorAttributeOptions.defaults()
      .including(
        ErrorAttributeOptions.Include.MESSAGE,
        ErrorAttributeOptions.Include.EXCEPTION
      )
}
