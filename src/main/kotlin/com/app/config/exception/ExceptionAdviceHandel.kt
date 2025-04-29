package com.app.config.exception

import com.app.data.RespBean
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.stereotype.Component
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.security.SignatureException

@Component
class ExceptionAdviceHandel {
  companion object {
    private val logger = LoggerFactory.getLogger(ExceptionAdviceHandel::class.java)
  }

  fun getDefaultExceptionResponse(req: HttpServletRequest, error: Throwable): RespBean<*> {
    return when (error) {
      is SignatureException -> signatureException(error)
      is MethodArgumentNotValidException -> methodArgumentExceptionAdvice(error)
      is BindException -> bindException(error)
      is HttpMessageNotReadableException -> httpMessageNotReadableExceptionAdvice(error)
      else -> throwableAdvice(error)
    }
  }

  /**
   * 处理通用异常
   */
  private fun throwableAdvice(error: Throwable): RespBean<Void> {
    logger.error("系统发生未处理异常", error)

    // 获取详细错误信息
    val printStackTrace = ByteArrayOutputStream()
    error.printStackTrace(PrintStream(printStackTrace))

    // 查找第一个应用包下的堆栈信息，用于定位错误
    val stackElement = error.stackTrace.firstOrNull {
      it.className.startsWith("com.app")
    }

    // 格式化错误信息
    val errorMsg = if (stackElement != null) {
      "系统异常: ${error.message ?: "Unknown error"} (位置: ${stackElement.className}:${stackElement.lineNumber})"
    } else {
      "系统异常: ${error.message ?: "Unknown error"}"
    }

    return RespBean.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMsg)
  }

  /**
   * 处理方法参数验证异常
   */
  private fun methodArgumentExceptionAdvice(error: MethodArgumentNotValidException): RespBean<Void> {
    logger.error("数据格式不正确：{}", error.message)
    val sb = StringBuilder()
    error.bindingResult.fieldErrors.forEach { errorInfo ->
      appendExceptionInfo(sb, errorInfo)
    }
    return RespBean.failure(HttpStatus.BAD_REQUEST.value(), sb.toString())
  }

  /**
   * 处理绑定异常
   */
  private fun bindException(error: BindException): RespBean<Void> {
    logger.error("数据格式不正确：{}", error.message)
    val sb = StringBuilder()
    error.bindingResult.fieldErrors.forEach { fieldError ->
      appendExceptionInfo(sb, fieldError)
    }
    return RespBean.failure(HttpStatus.BAD_REQUEST.value(), sb.toString())
  }

  /**
   * 添加异常信息到StringBuilder
   */
  private fun appendExceptionInfo(sb: StringBuilder, errorInfo: FieldError) {
    if (sb.isNotEmpty()) {
      sb.append("; ")
    }
    sb.append(errorInfo.field).append(": ").append(errorInfo.defaultMessage)
  }

  /**
   * 处理HTTP消息不可读异常
   */
  private fun httpMessageNotReadableExceptionAdvice(error: HttpMessageNotReadableException): RespBean<Void> {
    logger.error("参数数据类型不匹配：{}", error.message)
    return RespBean.failure(HttpStatus.BAD_REQUEST.value(), "参数数据类型不匹配: ${error.message}")
  }

  /**
   * 处理签名异常
   */
  private fun signatureException(error: SignatureException): RespBean<Void> {
    logger.info("签名异常", error)
    return RespBean.failure(420, error.message ?: "签名异常")
  }
}
