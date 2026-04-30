package com.cao.repairshop.serviceorder.service

import com.cao.repairshop.execution.repository.ExecutionRepository
import com.cao.repairshop.serviceorder.repository.ServiceOrderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServiceOrderMetricsServiceTest {

    private val serviceOrderRepository: ServiceOrderRepository = mockk()
    private val executionRepository: ExecutionRepository = mockk()
    private val service = ServiceOrderMetricsService(serviceOrderRepository, executionRepository)

    @Test
    fun `getMetrics should return average execution time from repository`() {
        val expectedAvg = 125.5
        every { serviceOrderRepository.getAverageExecutionTimeMinutes() } returns expectedAvg

        val response = service.getMetrics()

        assertEquals(expectedAvg, response.averageExecutionTimeMinutes)
        verify { serviceOrderRepository.getAverageExecutionTimeMinutes() }
    }

    @Test
    fun `getExecutionMetrics should return average execution time from execution repository`() {
        val expectedAvg = 45.0
        every { executionRepository.getAverageExecutionTimeMinutes() } returns expectedAvg

        val response = service.getExecutionMetrics()

        assertEquals(expectedAvg, response.averageExecutionTimeMinutes)
        verify { executionRepository.getAverageExecutionTimeMinutes() }
    }
}
