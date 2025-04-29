package com.app.config.satoken

import cn.dev33.satoken.context.SaHolder
import cn.dev33.satoken.filter.SaServletFilter
import cn.dev33.satoken.interceptor.SaInterceptor
import cn.dev33.satoken.jwt.StpLogicJwtForSimple
import cn.dev33.satoken.stp.StpLogic
import cn.dev33.satoken.util.SaResult
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * [Sa-Token 权限认证] 配置类
 * @author click33
 */
@Configuration
class SaTokenConfigure : WebMvcConfigurer {

  /**
   * 注册 Sa-Token 拦截器打开注解鉴权功能
   */
  override fun addInterceptors(registry: InterceptorRegistry) {
    // 注册 Sa-Token 拦截器打开注解鉴权功能
    registry.addInterceptor(SaInterceptor())
      .addPathPatterns("/**")
      .excludePathPatterns(
        "/api/auth/login",
        "/api/auth/register",
        "/api/sms",
        "/api/mobLogin",
        "/swagger/**",
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/doc.html",
        "/v2/**",
        "/v3/**",
        "/v2/api-docs/**",
        "/v2/api-docs-ext/**",
        "/v3/api-docs/**",
        "/favicon.ico",
        "/error"
      )
  }

  /**
   * Sa-Token 整合 jwt
   */
  @Bean
  fun getStpLogicJwt(): StpLogic {
    return StpLogicJwtForSimple()
  }

  /**
   * 注册 [Sa-Token 全局过滤器]
   */
  @Bean
  fun getSaServletFilter(): SaServletFilter {
    return SaServletFilter()
      // 指定 [拦截路由] 与 [放行路由]
      .addInclude("/**") // .addExclude("/favicon.ico")
      // 认证函数: 每次请求执行
      .setAuth {
        // println("---------- sa全局认证 ${SaHolder.getRequest().requestPath}")
      }
      // 异常处理函数：每次认证函数发生异常时执行此函数
      .setError { e ->
        println("---------- sa全局异常 ")
        e.printStackTrace()
        SaResult.error(e.message)
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
