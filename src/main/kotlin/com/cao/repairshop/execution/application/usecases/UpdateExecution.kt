package com.cao.repairshop.execution.application.usecases

import com.cao.repairshop.execution.infra.controller.dtos.ExecutionResponse
import com.cao.repairshop.execution.infra.controller.dtos.UpdateExecutionRequest
import java.util.UUID

fun interface UpdateExecution {
    fun execute(serviceOrderId: UUID, executionId: UUID, request: UpdateExecutionRequest): ExecutionResponse
}
