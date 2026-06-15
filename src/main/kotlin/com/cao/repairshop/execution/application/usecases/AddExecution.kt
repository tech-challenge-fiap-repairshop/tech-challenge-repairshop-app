package com.cao.repairshop.execution.application.usecases

import com.cao.repairshop.execution.infra.controller.dtos.CreateExecutionRequest
import com.cao.repairshop.execution.infra.controller.dtos.ExecutionResponse
import java.util.UUID

fun interface AddExecution {
    fun execute(serviceOrderId: UUID, request: CreateExecutionRequest): ExecutionResponse
}
