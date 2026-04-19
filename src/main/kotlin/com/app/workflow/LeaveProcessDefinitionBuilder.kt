package com.app.workflow

import com.app.data.dto.LeaveWorkflowNode
import org.flowable.bpmn.converter.BpmnXMLConverter
import org.flowable.bpmn.model.BpmnModel
import org.flowable.bpmn.model.EndEvent
import org.flowable.bpmn.model.FlowElement
import org.flowable.bpmn.model.ParallelGateway
import org.flowable.bpmn.model.Process as FlowableProcess
import org.flowable.bpmn.model.SequenceFlow
import org.flowable.bpmn.model.StartEvent
import org.flowable.bpmn.model.UserTask
import org.springframework.stereotype.Component

@Component
class LeaveProcessDefinitionBuilder {
  fun buildXml(
    processKey: String,
    processName: String,
    nodes: List<LeaveWorkflowNode>
  ): ByteArray {
    val model = BpmnModel()
    val process = FlowableProcess().apply {
      id = processKey
      name = processName
      isExecutable = true
    }
    model.addProcess(process)

    val startEvent = StartEvent().apply {
      id = "start"
      name = "开始"
    }
    process.addFlowElement(startEvent)

    var currentElementId = startEvent.id
    var flowSequence = 1

    nodes.forEachIndexed { nodeIndex, node ->
      if (node.parallel && node.approverRoleKeys.size > 1) {
        val split = ParallelGateway().apply {
          id = "parallel_split_${nodeIndex + 1}"
          name = "${node.nodeName}-并行开始"
        }
        val join = ParallelGateway().apply {
          id = "parallel_join_${nodeIndex + 1}"
          name = "${node.nodeName}-并行汇聚"
        }
        process.addFlowElement(split)
        process.addFlowElement(sequence("flow_${flowSequence++}", currentElementId, split.id))

        node.approverRoleKeys.forEachIndexed { roleIndex, roleKey ->
          val task = userTask(
            id = "task_${nodeIndex + 1}_${roleIndex + 1}",
            name = "${node.nodeName}-${roleKey}",
            roleKey = roleKey
          )
          process.addFlowElement(task)
          process.addFlowElement(sequence("flow_${flowSequence++}", split.id, task.id))
          process.addFlowElement(sequence("flow_${flowSequence++}", task.id, join.id))
        }
        process.addFlowElement(join)
        currentElementId = join.id
      } else {
        node.approverRoleKeys.forEachIndexed { roleIndex, roleKey ->
          val task = userTask(
            id = "task_${nodeIndex + 1}_${roleIndex + 1}",
            name = if (node.approverRoleKeys.size == 1) node.nodeName else "${node.nodeName}-${roleKey}",
            roleKey = roleKey
          )
          process.addFlowElement(task)
          process.addFlowElement(sequence("flow_${flowSequence++}", currentElementId, task.id))
          currentElementId = task.id
        }
      }
    }

    val endEvent = EndEvent().apply {
      id = "end"
      name = "结束"
    }
    process.addFlowElement(endEvent)
    process.addFlowElement(sequence("flow_${flowSequence++}", currentElementId, endEvent.id))

    return BpmnXMLConverter().convertToXML(model)
  }

  private fun userTask(
    id: String,
    name: String,
    roleKey: String
  ): UserTask {
    return UserTask().apply {
      this.id = id
      this.name = name
      this.candidateGroups = listOf(roleKey)
    }
  }

  private fun sequence(
    id: String,
    sourceRef: String,
    targetRef: String
  ): FlowElement {
    return SequenceFlow(sourceRef, targetRef).apply {
      this.id = id
    }
  }
}
