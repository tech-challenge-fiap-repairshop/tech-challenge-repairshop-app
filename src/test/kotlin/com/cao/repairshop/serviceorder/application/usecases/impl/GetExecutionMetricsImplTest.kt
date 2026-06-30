package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.execution.application.gateways.ExecutionGateway
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetExecutionMetricsImplTest {

    private lateinit var executionGateway: ExecutionGateway
    private lateinit var getExecutionMetricsImpl: GetExecutionMetricsImpl

    @BeforeEach
    fun setup() {
        executionGateway = mockk()
        getExecutionMetricsImpl = GetExecutionMetricsImpl(executionGateway)
    }

    @Test
    fun `should return execution metrics successfully`() {
        val expectedAvgMinutes = 45.5

        every { executionGateway.getAverageExecutionTimeMinutes() } returns expectedAvgMinutes

        val response = getExecutionMetricsImpl.execute()

        assertEquals(expectedAvgMinutes, response.averageExecutionTimeMinutes)
        verify { executionGateway.getAverageExecutionTimeMinutes() }
    }
}
