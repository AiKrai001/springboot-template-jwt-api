package com.app.config.satoken

import cn.dev33.satoken.context.SaHolder
import cn.dev33.satoken.filter.SaServletFilter
import cn.dev33.satoken.interceptor.SaInterceptor
import cn.dev33.satoken.jwt.StpLogicJwtForSimple
import cn.dev33.satoken.stp.StpLogic
import cn.dev33.satoken.stp.StpUtil
import com.app.data.RespBean
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Sa-Token 权限认证配置
 */
@Configuration
class SaTokenConfig(
  private val objectMapper: ObjectMapper
) : WebMvcConfigurer {

  companion object {
    private val log = LoggerFactory.getLogger(SaTokenConfig::class.java)

    // 排除静态资源和API文档相关，方法或类使用注解
    val excludePath = listOf(
      "/*.html",
      "/**/*.html",
      "/**/*.css",
      "/**/*.js",
      "/profile/**",
      "/favicon.ico",

      // API 文档相关
      "/doc.html/**",
      "/swagger-ui.html",
      "/swagger-resources/**",
      "/webjars/**",
      "/*/api-docs",
    )
  }

  override fun addInterceptors(registry: InterceptorRegistry) {
    // 打开注解鉴权功能,全局登录校验
    registry.addInterceptor(SaInterceptor { _ -> StpUtil.checkLogin() })
      .addPathPatterns("/**")
      .excludePathPatterns(excludePath)
  }

  /** Sa-Token 整合 jwt */
  @Bean
  fun getStpLogicJwt(): StpLogic {
    return StpLogicJwtForSimple()
  }

  /** Sa-Token 全局过滤器（仅做通用 header/错误包装） */
  @Bean
  fun getSaServletFilter(): SaServletFilter {
    return SaServletFilter()
      .addInclude("/**").addExclude("/favicon.ico")
      // 登录校验改由 LoginCheckInterceptor 处理
      .setAuth { }
      // 认证异常的统一返回（仅处理 setAuth 内抛出的异常）
      .setError { e ->
        SaHolder.getResponse().setStatus(401)
        SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=utf-8")
        val path = SaHolder.getRequest().requestPath
        log.info("请求地址'{}', Sa Token 认证失败: {}", path, e.message)
        return@setError objectMapper.writeValueAsString(
          RespBean.unauthorized<String>(
            e.message ?: "未登录或 token 无效"
          )
        )
      }
      // 前置函数：在每次认证函数之前执行
      .setBeforeAuth {
        // ---------- 设置一些安全响应头 ----------
        SaHolder.getResponse()
          // 服务器名称
          .setServer("sa-server")
          // 是否可以在iframe显示视图： DENY=不可以 | SAMEORIGIN=同域下可以 | ALLOW-FROM uri=指定域名下可以
          .setHeader("X-Frame-Options", "SAMEORIGIN")
          // 是否启用浏览器默认XSS防护： 0=禁用 | 1=启用 | 1; mode=block 启用, 并在检查到XSS攻击时，停止渲染页面
          .setHeader("X-XSS-Protection", "1; mode=block")
          // 禁用浏览器内容嗅探
          .setHeader("X-Content-Type-Options", "nosniff")
      }
  }
}

