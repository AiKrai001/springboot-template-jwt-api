package com.app.config.exception

import com.app.data.RespBean
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody

/**
 * 全局异常统一处理
 */
@ControllerAdvice
@ResponseBody
class GlobalException(
  private val exceptionAdviceHandel: ExceptionAdviceHandel
) {
  
  // 所有未明确处理的异常将通过exceptionAdviceHandel统一处理
  @ExceptionHandler(Throwable::class)
  fun handleAllExceptions(req: HttpServletRequest, error: Throwable): RespBean<*> {
    log.error("全局异常处理", error)
    return exceptionAdviceHandel.getDefaultExceptionResponse(req, error)
  }

  // 验证相关异常处理
  @ExceptionHandler(ValidationException::class)
  fun validateError(exception: ValidationException): RespBean<Void> {
    log.warn("验证不通过: [{}: {}]", exception.javaClass.name, exception.message)
    return RespBean.failure(HttpStatus.BAD_REQUEST.value(), "请求参数有误: ${exception.message}")
  }

  // 处理参数校验异常
  @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class)
  fun handleBindingErrors(req: HttpServletRequest, error: Exception): RespBean<*> {
    log.warn("参数校验异常", error)
    return exceptionAdviceHandel.getDefaultExceptionResponse(req, error)
  }

  // 处理HTTP消息转换异常
  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleHttpMessageNotReadableException(req: HttpServletRequest, error: HttpMessageNotReadableException): RespBean<*> {
    log.warn("HTTP消息转换异常", error)
    return exceptionAdviceHandel.getDefaultExceptionResponse(req, error)
  }

  companion object {
    private val log = LoggerFactory.getLogger(GlobalException::class.java)
  }
}
