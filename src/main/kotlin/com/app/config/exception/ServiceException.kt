package com.app.config.exception

/**
 * 业务异常
 */
data class ServiceException(override val message: String? = null, val code: Int? = null) : RuntimeException()
