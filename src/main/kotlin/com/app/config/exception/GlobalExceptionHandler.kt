package com.app.config.exception

import com.app.data.RespBean
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * 全局异常统一处理
 */
@RestControllerAdvice
class GlobalExceptionHandler() {
  companion object {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
  }

  /**
   * 业务异常
   */
  @ExceptionHandler(ServiceException::class)
  fun handleServiceException(e: ServiceException, request: HttpServletRequest?): RespBean<Void> {
    log.error(e.message, e)
    val code: Int? = e.code
    return if (code != null) RespBean.failure(code, e.message) else RespBean.failure(e.message)
  }

  /**
   * 权限校验异常
   */
  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException, request: HttpServletRequest): RespBean<Void> {
    val requestURI = request.requestURI
    log.error("请求地址'{}',权限校验失败'{}'", requestURI, e.message)
    return RespBean.failure(HttpStatus.FORBIDDEN.value(), "没有权限，请联系管理员授权")
  }

  /**
   * 请求方式不支持
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
  fun handleHttpRequestMethodNotSupported(
    e: HttpRequestMethodNotSupportedException,
    request: HttpServletRequest
  ): RespBean<Void> {
    val requestURI = request.requestURI
    log.error("请求地址'{}',不支持'{}'请求", requestURI, e.method)
    return RespBean.failure(e.message)
  }

  /**
   * 请求路径中缺少必需的路径变量
   */
  @ExceptionHandler(MissingPathVariableException::class)
  fun handleMissingPathVariableException(e: MissingPathVariableException, request: HttpServletRequest): RespBean<Void> {
    val requestURI = request.requestURI
    log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI, e)
    return RespBean.failure(String.format("请求路径中缺少必需的路径变量[%s]", e.variableName))
  }

  /**
   * 请求参数类型不匹配
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleMethodArgumentTypeMismatchException(
    e: MethodArgumentTypeMismatchException,
    request: HttpServletRequest
  ): RespBean<Void> {
    val requestURI = request.requestURI
    log.error("请求参数类型不匹配'{}',发生系统异常.", requestURI, e)
    return RespBean.failure(
      String.format(
        "请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'",
        e.name,
        e.requiredType!!.getName(),
        e.value
      )
    )
  }

  /**
   * 拦截未知的运行时异常
   */
  @ExceptionHandler(RuntimeException::class)
  fun handleRuntimeException(e: RuntimeException, request: HttpServletRequest): RespBean<Void> {
    val requestURI = request.requestURI
    log.error("请求地址'{}',发生未知异常.", requestURI, e)
    return RespBean.failure(e.message)
  }

  /**
   * 系统异常
   */
  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception, request: HttpServletRequest): RespBean<Void> {
    val requestURI = request.requestURI
    log.error("请求地址'{}',发生系统异常.", requestURI, e)
    return RespBean.failure(e.message)
  }

  /**
   * 自定义验证异常
   */
  @ExceptionHandler(BindException::class)
  fun handleBindException(e: BindException): RespBean<Void> {
    log.error(e.message, e)
    val message = e.allErrors[0].defaultMessage
    return RespBean.failure(message)
  }

  /**
   * 自定义验证异常
   */
  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): RespBean<Void> {
    log.error(e.message, e)
    val message = e.bindingResult.fieldError!!.defaultMessage
    return RespBean.failure(message)
  }

}
