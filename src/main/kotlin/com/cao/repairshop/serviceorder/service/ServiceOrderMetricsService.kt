package com.cao.repairshop.serviceorder.service

import com.cao.repairshop.serviceorder.dto.ServiceOrderMetricsResponse
import com.cao.repairshop.serviceorder.repository.ServiceOrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServiceOrderMetricsService(
    private val serviceOrderRepository: ServiceOrderRepository
) {

    @Transactional(readOnly = true)
    fun getMetrics(): ServiceOrderMetricsResponse {
        val avgMinutes = serviceOrderRepository.getAverageExecutionTimeMinutes()
        return ServiceOrderMetricsResponse(averageExecutionTimeMinutes = avgMinutes)
    }
}
