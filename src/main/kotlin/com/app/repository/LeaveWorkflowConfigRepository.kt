package com.app.repository

import com.app.data.model.LeaveWorkflowConfig
import com.app.data.model.configId
import com.app.data.model.enabled
import com.app.data.model.processKey
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq

interface LeaveWorkflowConfigRepository : KRepository<LeaveWorkflowConfig, Long> {
  fun findEnabledConfigs(): List<LeaveWorkflowConfig> =
    sql.createQuery(LeaveWorkflowConfig::class) {
      where(table.enabled eq true)
      select(table)
    }.execute()

  fun findByProcessKeyValue(key: String): LeaveWorkflowConfig? =
    sql.createQuery(LeaveWorkflowConfig::class) {
      where(table.processKey eq key)
      select(table)
    }.execute().firstOrNull()

  fun findByConfigId(id: Long): LeaveWorkflowConfig? =
    sql.createQuery(LeaveWorkflowConfig::class) {
      where(table.configId eq id)
      select(table)
    }.execute().firstOrNull()
}
