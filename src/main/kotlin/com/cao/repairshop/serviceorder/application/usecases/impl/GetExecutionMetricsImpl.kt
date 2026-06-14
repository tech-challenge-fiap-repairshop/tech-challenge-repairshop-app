package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.execution.application.gateways.ExecutionGateway
import com.cao.repairshop.execution.infra.controller.dtos.ExecutionMetricsResponse
import com.cao.repairshop.serviceorder.application.usecases.GetExecutionMetrics
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetExecutionMetricsImpl(
    private val executionGateway: ExecutionGateway
) : GetExecutionMetrics {

    @Transactional(readOnly = true)
    override fun execute(): ExecutionMetricsResponse {
        val avgMinutes = executionGateway.getAverageExecutionTimeMinutes()
        return ExecutionMetricsResponse(averageExecutionTimeMinutes = avgMinutes)
    }
}
