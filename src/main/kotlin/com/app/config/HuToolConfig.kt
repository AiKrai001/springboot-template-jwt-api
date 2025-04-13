package com.app.config

import cn.hutool.core.lang.Snowflake
import cn.hutool.core.util.IdUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HuToolConfig {

  @Bean
  fun snowflake(): Snowflake {
    return IdUtil.getSnowflake()
  }
}