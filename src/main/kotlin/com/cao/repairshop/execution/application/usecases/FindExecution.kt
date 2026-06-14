package com.cao.repairshop.execution.application.usecases

import com.cao.repairshop.execution.infra.controller.dtos.ExecutionResponse
import java.util.UUID

interface FindExecution {
    fun execute(serviceOrderId: UUID, executionId: UUID): ExecutionResponse
}
