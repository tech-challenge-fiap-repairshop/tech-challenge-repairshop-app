package com.cao.repairshop.serviceorder.infra.gateways

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.register.infra.persistence.models.CustomerEntity
import com.cao.repairshop.register.infra.persistence.models.VehicleEntity
import com.cao.repairshop.register.infra.persistence.repositories.CustomerRepository
import com.cao.repairshop.register.infra.persistence.repositories.VehicleRepository
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import com.cao.repairshop.serviceorder.infra.persistence.repositories.ServiceOrderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID

class ServiceOrderGatewayImplJPATest {

    private lateinit var serviceOrderRepository: ServiceOrderRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var serviceOrderGatewayImplJPA: ServiceOrderGatewayImplJPA

    @BeforeEach
    fun setup() {
        serviceOrderRepository = mockk()
        customerRepository = mockk()
        vehicleRepository = mockk()
        serviceOrderGatewayImplJPA = ServiceOrderGatewayImplJPA(serviceOrderRepository, customerRepository, vehicleRepository)
    }

    @Test
    fun `should save service order successfully`() {
        val customerId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val serviceOrder = mockk<ServiceOrder>(relaxed = true)
        every { serviceOrder.customerId } returns customerId
        every { serviceOrder.vehicleId } returns vehicleId
        
        val customerEntity = mockk<CustomerEntity>(relaxed = true)
        val vehicleEntity = mockk<VehicleEntity>(relaxed = true)
        val serviceOrderEntity = mockk<ServiceOrderEntity>(relaxed = true)
        
        every { customerRepository.findById(customerId) } returns Optional.of(customerEntity)
        every { vehicleRepository.findById(vehicleId) } returns Optional.of(vehicleEntity)
        every { serviceOrderRepository.save(any()) } returns serviceOrderEntity
        every { serviceOrderEntity.id } returns UUID.randomUUID()
        every { serviceOrderEntity.customer.id } returns customerId
        every { serviceOrderEntity.vehicle.id } returns vehicleId

        val result = serviceOrderGatewayImplJPA.save(serviceOrder)
        assertNotNull(result)
        verify { serviceOrderRepository.save(any()) }
    }

    @Test
    fun `should throw EntityNotFoundException when saving with invalid customer`() {
        val customerId = UUID.randomUUID()
        val serviceOrder = mockk<ServiceOrder>(relaxed = true)
        every { serviceOrder.customerId } returns customerId

        every { customerRepository.findById(customerId) } returns Optional.empty()

        assertThrows(EntityNotFoundException::class.java) {
            serviceOrderGatewayImplJPA.save(serviceOrder)
        }
    }

    @Test
    fun `should throw EntityNotFoundException when saving with invalid vehicle`() {
        val customerId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val serviceOrder = mockk<ServiceOrder>(relaxed = true)
        every { serviceOrder.customerId } returns customerId
        every { serviceOrder.vehicleId } returns vehicleId

        val customerEntity = mockk<CustomerEntity>(relaxed = true)
        every { customerRepository.findById(customerId) } returns Optional.of(customerEntity)
        every { vehicleRepository.findById(vehicleId) } returns Optional.empty()

        assertThrows(EntityNotFoundException::class.java) {
            serviceOrderGatewayImplJPA.save(serviceOrder)
        }
    }

    @Test
    fun `should find detailed by id successfully`() {
        val id = UUID.randomUUID()
        val serviceOrderEntity = mockk<ServiceOrderEntity>(relaxed = true)
        every { serviceOrderEntity.id } returns id
        every { serviceOrderEntity.customer.id } returns UUID.randomUUID()
        every { serviceOrderEntity.vehicle.id } returns UUID.randomUUID()
        every { serviceOrderRepository.findDetailedById(id) } returns Optional.of(serviceOrderEntity)

        val result = serviceOrderGatewayImplJPA.findDetailedById(id)
        assertNotNull(result)
        assertEquals(id, result?.id)
    }

    @Test
    fun `should find all successfully`() {
        val pageable = PageRequest.of(0, 10)
        val serviceOrderEntity = mockk<ServiceOrderEntity>(relaxed = true)
        every { serviceOrderEntity.customer.id } returns UUID.randomUUID()
        every { serviceOrderEntity.vehicle.id } returns UUID.randomUUID()
        every { serviceOrderRepository.findAll(pageable) } returns PageImpl(listOf(serviceOrderEntity))

        val result = serviceOrderGatewayImplJPA.findAll(pageable)
        assertEquals(1, result.totalElements)
    }

    @Test
    fun `should check if exists by customer id`() {
        val customerId = UUID.randomUUID()
        every { serviceOrderRepository.existsByCustomerId(customerId) } returns true
        assertTrue(serviceOrderGatewayImplJPA.existsByCustomerId(customerId))
    }

    @Test
    fun `should check if exists by vehicle id`() {
        val vehicleId = UUID.randomUUID()
        every { serviceOrderRepository.existsByVehicleId(vehicleId) } returns true
        assertTrue(serviceOrderGatewayImplJPA.existsByVehicleId(vehicleId))
    }

    @Test
    fun `should get average execution time minutes`() {
        every { serviceOrderRepository.getAverageExecutionTimeMinutes() } returns 120.0
        val result = serviceOrderGatewayImplJPA.getAverageExecutionTimeMinutes()
        assertEquals(120.0, result)
    }
}
