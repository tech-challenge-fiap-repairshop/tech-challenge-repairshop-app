package com.cao.repairshop.execution.application.usecases

import com.cao.repairshop.execution.infra.controller.dtos.ExecutionResponse
import java.util.UUID

fun interface FindExecution {
    fun findById(serviceOrderId: UUID, executionId: UUID): ExecutionResponse
}
