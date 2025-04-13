package com.app.utlis

/**
 * 一些常量字符串整合
 */
object Const {
  // JWT令牌
  const val JWT_BLACK_LIST = "jwt:blacklist:"
  const val JWT_FREQUENCY = "jwt:frequency:"

  // 请求频率限制
  const val FLOW_LIMIT_COUNTER = "flow:counter:"
  const val FLOW_LIMIT_BLOCK = "flow:block:"

  // 邮件验证码
  const val VERIFY_EMAIL_LIMIT = "verify:email:limit:"
  const val VERIFY_EMAIL_DATA = "verify:email:data:"

  // 过滤器优先级
  const val ORDER_FLOW_LIMIT = -101
  const val ORDER_CORS = -102

  // 请求自定义属性
  const val ATTR_USER_ID = "userId"

  // 消息队列
  const val MQ_MAIL = "mail"

  // 用户角色
  const val ROLE_DEFAULT = "user"
}
