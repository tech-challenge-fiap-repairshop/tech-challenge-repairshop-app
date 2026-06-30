package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetServiceOrderMetricsImplTest {

    private lateinit var serviceOrderGateway: ServiceOrderGateway
    private lateinit var getServiceOrderMetricsImpl: GetServiceOrderMetricsImpl

    @BeforeEach
    fun setup() {
        serviceOrderGateway = mockk()
        getServiceOrderMetricsImpl = GetServiceOrderMetricsImpl(serviceOrderGateway)
    }

    @Test
    fun `should return service order metrics successfully`() {
        val expectedAvgMinutes = 120.0

        every { serviceOrderGateway.getAverageExecutionTimeMinutes() } returns expectedAvgMinutes

        val response = getServiceOrderMetricsImpl.execute()

        assertEquals(expectedAvgMinutes, response.averageExecutionTimeMinutes)
        verify { serviceOrderGateway.getAverageExecutionTimeMinutes() }
    }
}
