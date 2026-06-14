package com.cao.repairshop.execution.application.usecases

import com.cao.repairshop.execution.infra.controller.dtos.CreateExecutionBatchRequest
import com.cao.repairshop.execution.infra.controller.dtos.ExecutionResponse
import java.util.UUID

interface AddExecutionBatch {
    fun execute(serviceOrderId: UUID, request: CreateExecutionBatchRequest): List<ExecutionResponse>
}
