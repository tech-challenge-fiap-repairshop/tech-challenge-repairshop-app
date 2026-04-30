package com.cao.repairshop.serviceorder.service

import com.cao.repairshop.serviceorder.repository.ServiceOrderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class ServiceOrderExistenceAdapterTest {

    private val repository: ServiceOrderRepository = mockk()
    private val adapter = ServiceOrderExistenceAdapter(repository)

    @Test
    fun `existsByCustomerId should call repository and return result`() {
        val customerId = UUID.randomUUID()
        every { repository.existsByCustomerId(customerId) } returns true

        val result = adapter.existsByCustomerId(customerId)

        assertTrue(result)
        verify { repository.existsByCustomerId(customerId) }
    }

    @Test
    fun `existsByCustomerId should return false if repository returns false`() {
        val customerId = UUID.randomUUID()
        every { repository.existsByCustomerId(customerId) } returns false

        val result = adapter.existsByCustomerId(customerId)

        assertFalse(result)
        verify { repository.existsByCustomerId(customerId) }
    }

    @Test
    fun `existsByVehicleId should call repository and return result`() {
        val vehicleId = UUID.randomUUID()
        every { repository.existsByVehicleId(vehicleId) } returns true

        val result = adapter.existsByVehicleId(vehicleId)

        assertTrue(result)
        verify { repository.existsByVehicleId(vehicleId) }
    }

    @Test
    fun `existsByVehicleId should return false if repository returns false`() {
        val vehicleId = UUID.randomUUID()
        every { repository.existsByVehicleId(vehicleId) } returns false

        val result = adapter.existsByVehicleId(vehicleId)

        assertFalse(result)
        verify { repository.existsByVehicleId(vehicleId) }
    }
}
