package com.cao.repairshop.serviceorder.service

import com.cao.repairshop.serviceorder.dto.ServiceOrderMetricsResponse
import com.cao.repairshop.execution.dto.ExecutionMetricsResponse
import com.cao.repairshop.serviceorder.repository.ServiceOrderRepository
import com.cao.repairshop.execution.repository.ExecutionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServiceOrderMetricsService(
    private val serviceOrderRepository: ServiceOrderRepository,
    private val executionRepository: ExecutionRepository
) {

    @Transactional(readOnly = true)
    fun getMetrics(): ServiceOrderMetricsResponse {
        val avgMinutes = serviceOrderRepository.getAverageExecutionTimeMinutes()
        return ServiceOrderMetricsResponse(averageExecutionTimeMinutes = avgMinutes)
    }

    @Transactional(readOnly = true)
    fun getExecutionMetrics(): ExecutionMetricsResponse {
        val avgMinutes = executionRepository.getAverageExecutionTimeMinutes()
        return ExecutionMetricsResponse(averageExecutionTimeMinutes = avgMinutes)
    }
}
