package com.cao.repairshop.execution.application.gateways

import com.cao.repairshop.execution.domain.entities.Execution
import java.util.UUID

interface ExecutionGateway {
    fun save(execution: Execution): Execution
    fun findById(id: UUID): Execution?
    fun findByServiceOrderId(serviceOrderId: UUID): List<Execution>
    fun delete(id: UUID)
    fun getAverageExecutionTimeMinutes(): Double?
}
