package com.app.data.model

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table
import java.sql.Timestamp

@Entity
@Table(name = "leave_workflow_config")
interface LeaveWorkflowConfig {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val configId: Long

  val configName: String
  val priority: Int
  val enabled: Boolean
  val conditionJson: String
  val nodeJson: String
  val processKey: String?
  val processDefinitionId: String?
  val remark: String?
  val createTime: Timestamp?
  val updateTime: Timestamp?
}
