package com.app.utlis

import jakarta.annotation.Resource
import lombok.extern.slf4j.Slf4j
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * 限流通用工具
 * 针对于不同的情况进行限流操作，支持限流升级
 */
@Slf4j
@Component
class FlowUtils {

  @Resource
  private lateinit var template: StringRedisTemplate

  /**
   * 针对于单次频率限制，请求成功后，在冷却时间内不得再次进行请求，如3秒内不能再次发起请求
   * @param key 键
   * @param blockTime 限制时间
   * @return 是否通过限流检查
   */
  fun limitOnceCheck(key: String, blockTime: Int): Boolean {
    return internalCheck(key, 1, blockTime) { false }
  }

  /**
   * 针对于单次频率限制，请求成功后，在冷却时间内不得再次进行请求
   * 如3秒内不能再次发起请求，如果不听劝阻继续发起请求，将限制更长时间
   * @param key 键
   * @param frequency 请求频率
   * @param baseTime 基础限制时间
   * @param upgradeTime 升级限制时间
   * @return 是否通过限流检查
   */
  fun limitOnceUpgradeCheck(key: String, frequency: Int, baseTime: Int, upgradeTime: Int): Boolean {
    return internalCheck(key, frequency, baseTime) { overclock ->
      if (overclock) {
        template.opsForValue().set(key, "1", upgradeTime.toLong(), TimeUnit.SECONDS)
      }
      false
    }
  }

  /**
   * 针对于在时间段内多次请求限制，如3秒内限制请求20次，超出频率则封禁一段时间
   * @param counterKey 计数键
   * @param blockKey 封禁键
   * @param blockTime 封禁时间
   * @param frequency 请求频率
   * @param period 计数周期
   * @return 是否通过限流检查
   */
  fun limitPeriodCheck(counterKey: String, blockKey: String, blockTime: Int, frequency: Int, period: Int): Boolean {
    return internalCheck(counterKey, frequency, period) { overclock ->
      if (overclock) {
        template.opsForValue().set(blockKey, "", blockTime.toLong(), TimeUnit.SECONDS)
      }
      !overclock
    }
  }

  /**
   * 内部使用请求限制主要逻辑
   * @param key 计数键
   * @param frequency 请求频率
   * @param period 计数周期
   * @param action 限制行为与策略
   * @return 是否通过限流检查
   */
  private fun internalCheck(key: String, frequency: Int, period: Int, action: (Boolean) -> Boolean): Boolean {
    val count = template.opsForValue().get(key)
    return if (count != null) {
      val value = template.opsForValue().increment(key) ?: 0L
      val c = count.toInt()
      if (value != c + 1L) {
        template.expire(key, period.toLong(), TimeUnit.SECONDS)
      }
      action(value > frequency)
    } else {
      template.opsForValue().set(key, "1", period.toLong(), TimeUnit.SECONDS)
      true
    }
  }

  companion object {
    private val log = org.slf4j.LoggerFactory.getLogger(FlowUtils::class.java)
  }
}
