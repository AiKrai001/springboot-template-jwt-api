package com.app.config.exception

import cn.dev33.satoken.exception.NotPermissionException
import cn.dev33.satoken.exception.NotRoleException
import cn.dev33.satoken.exception.SaTokenException
import com.app.data.RespBean
import com.app.logging.HttpLogSupport
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

/**
 * 全局异常统一处理
 */
@RestControllerAdvice
class GlobalExceptionHandler() {
  companion object {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
  }

  @ExceptionHandler(SaTokenException::class)
  fun handleSaTokenException(e: SaTokenException, request: HttpServletRequest): ResponseEntity<RespBean<Void>> {
    logHandledException(
      request = request,
      status = HttpStatus.UNAUTHORIZED.value(),
      exception = e,
      action = "auth.unauthorized",
      warnOnly = true
    )
    return failureResponse(HttpStatus.UNAUTHORIZED, e.message ?: "未登录或 token 无效")
  }

  /**
   * 业务异常
   */
  @ExceptionHandler(ServiceException::class)
  fun handleServiceException(e: ServiceException, request: HttpServletRequest?): ResponseEntity<RespBean<Void>> {
    val code: Int? = e.code
    val status = code?.let(HttpStatusCode::valueOf) ?: HttpStatus.INTERNAL_SERVER_ERROR
    logHandledException(
      request = request,
      status = status.value(),
      exception = e,
      action = "business.failure",
      warnOnly = code != null && code < 500
    )
    return if (code != null) {
      ResponseEntity.status(status).body(RespBean.failure(code, e.message))
    } else {
      failureResponse(status, e.message)
    }
  }

  /**
   * 权限校验异常
   */
  @ExceptionHandler(value = [NotPermissionException::class, NotRoleException::class])
  fun handleAccessDeniedException(e: SaTokenException, request: HttpServletRequest): ResponseEntity<RespBean<Void>> {
    logHandledException(
      request = request,
      status = HttpStatus.FORBIDDEN.value(),
      exception = e,
      action = "auth.forbidden",
      warnOnly = true
    )
    return failureResponse(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权")
  }

  /**
   * 请求方式不支持
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
  fun handleHttpRequestMethodNotSupported(
    e: HttpRequestMethodNotSupportedException,
    request: HttpServletRequest
  ): ResponseEntity<RespBean<Void>> {
    logHandledException(
      request = request,
      status = HttpStatus.METHOD_NOT_ALLOWED.value(),
      exception = e,
      action = "request.method_not_allowed",
      warnOnly = true
    )
    return failureResponse(HttpStatus.METHOD_NOT_ALLOWED, e.message)
  }

  @ExceptionHandler(NoResourceFoundException::class)
  fun handleNoResourceFoundException(
    e: NoResourceFoundException,
    request: HttpServletRequest
  ): ResponseEntity<RespBean<Void>> {
    logHandledException(
      request = request,
      status = HttpStatus.NOT_FOUND.value(),
      exception = e,
      action = "request.not_found",
      warnOnly = true
    )
    return failureResponse(HttpStatus.NOT_FOUND, e.message)
  }

  /**
   * 请求路径中缺少必需的路径变量
   */
  @ExceptionHandler(MissingPathVariableException::class)
  fun handleMissingPathVariableException(
    e: MissingPathVariableException,
    request: HttpServletRequest
  ): ResponseEntity<RespBean<Void>> {
    logHandledException(
      request = request,
      status = HttpStatus.BAD_REQUEST.value(),
      exception = e,
      action = "request.path_variable_missing",
      warnOnly = true
    )
    return failureResponse(
      HttpStatus.BAD_REQUEST,
      String.format("请求路径中缺少必需的路径变量[%s]", e.variableName)
    )
  }

  /**
   * 请求参数类型不匹配
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleMethodArgumentTypeMismatchException(
    e: MethodArgumentTypeMismatchException,
    request: HttpServletRequest
  ): ResponseEntity<RespBean<Void>> {
    logHandledException(
      request = request,
      status = HttpStatus.BAD_REQUEST.value(),
      exception = e,
      action = "request.argument_type_mismatch",
      warnOnly = true
    )
    return failureResponse(
      HttpStatus.BAD_REQUEST,
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
  fun handleRuntimeException(e: RuntimeException, request: HttpServletRequest): ResponseEntity<RespBean<Void>> {
    logHandledException(
      request = request,
      status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
      exception = e,
      action = "request.runtime_error"
    )
    return failureResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
  }

  /**
   * 系统异常
   */
  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception, request: HttpServletRequest): ResponseEntity<RespBean<Void>> {
    logHandledException(
      request = request,
      status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
      exception = e,
      action = "request.internal_error"
    )
    return failureResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
  }

  /**
   * 自定义验证异常
   */
  @ExceptionHandler(BindException::class)
  fun handleBindException(e: BindException, request: HttpServletRequest): ResponseEntity<RespBean<Void>> {
    logHandledException(
      request = request,
      status = HttpStatus.BAD_REQUEST.value(),
      exception = e,
      action = "request.bind_error",
      warnOnly = true
    )
    val message = e.allErrors[0].defaultMessage
    return failureResponse(HttpStatus.BAD_REQUEST, message)
  }

  /**
   * 自定义验证异常
   */
  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(
    e: MethodArgumentNotValidException,
    request: HttpServletRequest
  ): ResponseEntity<RespBean<Void>> {
    logHandledException(
      request = request,
      status = HttpStatus.BAD_REQUEST.value(),
      exception = e,
      action = "request.validation_failed",
      warnOnly = true
    )
    val message = e.bindingResult.fieldError!!.defaultMessage
    return failureResponse(HttpStatus.BAD_REQUEST, message)
  }

  private fun failureResponse(
    status: HttpStatusCode,
    message: String?
  ): ResponseEntity<RespBean<Void>> {
    return ResponseEntity.status(status).body(RespBean.failure(status.value(), message))
  }

  private fun logHandledException(
    request: HttpServletRequest?,
    status: Int,
    exception: Exception,
    action: String,
    warnOnly: Boolean = false
  ) {
    val loggingEvent = if (warnOnly) log.atWarn() else log.atError()
    val method = request?.method ?: "UNKNOWN"
    val path = request?.requestURI ?: "unknown"
    val clientIp = request?.let(HttpLogSupport::resolveClientIp) ?: "unknown"

    loggingEvent
      .setCause(exception)
      .addKeyValue("event.action", action)
      .addKeyValue("event.category", "error")
      .addKeyValue("event.outcome", "failure")
      .addKeyValue("http.request.method", method)
      .addKeyValue("url.path", path)
      .addKeyValue("client.address", clientIp)
      .addKeyValue("user.id", HttpLogSupport.currentUserId())
      .addKeyValue("http.response.status_code", status)
      .log("HTTP request failed")
  }
}
