package com.cao.repairshop.serviceorder.application.usecases

import com.cao.repairshop.execution.infra.controller.dtos.ExecutionMetricsResponse

interface GetExecutionMetrics {
    fun execute(): ExecutionMetricsResponse
}
