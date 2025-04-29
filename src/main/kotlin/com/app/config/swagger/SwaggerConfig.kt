package com.app.config.swagger

import cn.hutool.core.util.RandomUtil
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

  @Bean
  fun orderGlobalOpenApiCustomizer(): GlobalOpenApiCustomizer {
    return GlobalOpenApiCustomizer { openApi ->
      openApi.tags?.forEach { tag ->
        val map = HashMap<String, Any>()
        map["x-order"] = RandomUtil.randomInt(0, 100)
        tag.extensions = map
      }

      if (openApi.paths != null) {
        openApi.addExtension("x-test123", "333")
        openApi.paths.addExtension("x-abb", RandomUtil.randomInt(1, 100))
      }
    }
  }

  @Bean
  fun customOpenAPI(): OpenAPI {
    return OpenAPI()
      .info(
        Info()
          .title("XXX用户系统API")
          .version("1.0")
          .description("Knife4j集成springdoc-openapi示例")
          .termsOfService("http://doc.xiaominfo.com")
          .license(
            License().name("Apache 2.0")
              .url("http://doc.xiaominfo.com")
          )
      )
  }

//  @Bean
//  fun csrsApi(): GroupedOpenApi {
//    return GroupedOpenApi.builder()
//      .group("csrs系统模块")
//      .packagesToScan("com.csrs.web.controller.csrs")
//      .build()
//  }
//
//  @Bean
//  fun aiApi(): GroupedOpenApi {
//    return GroupedOpenApi.builder()
//      .group("ai模块")
//      .packagesToScan("com.csrs.web.controller.ai")
//      .build()
//  }
}
