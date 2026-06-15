package com.cao.repairshop.execution.application.usecases

import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.execution.infra.controller.dtos.ExecutionResponse
import java.util.UUID

fun interface AdvanceExecutionStatus {
    fun execute(serviceOrderId: UUID, executionId: UUID, newStatus: ExecutionStatus): ExecutionResponse
}
