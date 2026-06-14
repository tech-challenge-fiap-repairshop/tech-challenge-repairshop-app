package com.cao.repairshop.serviceorder.application.usecases

import com.cao.repairshop.execution.infra.controller.dtos.ExecutionMetricsResponse

fun interface GetExecutionMetrics {
    fun execute(): ExecutionMetricsResponse
}
