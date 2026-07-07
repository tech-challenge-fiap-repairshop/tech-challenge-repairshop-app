package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.inventory.application.gateways.InsumeGateway
import com.cao.repairshop.inventory.domain.entities.Insume
import com.cao.repairshop.register.application.gateways.CustomerGateway
import com.cao.repairshop.register.application.gateways.VehicleGateway
import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Document
import com.cao.repairshop.register.domain.entities.Plate
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.application.usecases.NotifyCustomer
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.infra.controller.dtos.CreateServiceOrderRequest
import com.cao.repairshop.serviceorder.infra.controller.dtos.ExecutionDefinitionRequest
import com.cao.repairshop.serviceorder.infra.controller.dtos.InsumeItemRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class CreateServiceOrderImplTest {

    private lateinit var serviceOrderGateway: ServiceOrderGateway
    private lateinit var customerGateway: CustomerGateway
    private lateinit var vehicleGateway: VehicleGateway
    private lateinit var insumeGateway: InsumeGateway
    private lateinit var notifyCustomer: NotifyCustomer
    private lateinit var createServiceOrderImpl: CreateServiceOrderImpl

    @BeforeEach
    fun setup() {
        serviceOrderGateway = mockk()
        customerGateway = mockk()
        vehicleGateway = mockk()
        insumeGateway = mockk()
        notifyCustomer = mockk(relaxed = true)
        createServiceOrderImpl = CreateServiceOrderImpl(
            serviceOrderGateway, customerGateway, vehicleGateway, insumeGateway, notifyCustomer
        )
    }

    @Test
    fun `should create service order successfully`() {
        val request = CreateServiceOrderRequest(
            customerEmail = "test@example.com",
            vehiclePlate = "ABC-1234",
            services = listOf(
                ExecutionDefinitionRequest(
                    basicDescription = "OIL_CHANGE",
                    fullDescription = "Oil change 5W40",
                    price = BigDecimal("100.00"),
                    insumes = listOf(
                        InsumeItemRequest(insumeId = UUID.randomUUID(), quantity = 2)
                    )
                )
            )
        )

        val customer = Customer(name = "John", document = Document("12345678909"))
        val vehicle = Vehicle(brand = "Ford", model = "Fiesta", plate = Plate("ABC-1234"), customerId = customer.id)
        val insume = Insume(id = request.services.first().insumes.first().insumeId, name = "Oil", price = BigDecimal.TEN, unityPrice = BigDecimal.TEN)

        every { customerGateway.findByEmail(any()) } returns customer
        every { vehicleGateway.findByPlate(any()) } returns vehicle
        every { insumeGateway.findById(any()) } returns insume
        every { serviceOrderGateway.save(any()) } answers { firstArg() }

        val response = createServiceOrderImpl.execute(request)

        assertEquals(customer.id, response.customerId)
        assertEquals(vehicle.id, response.vehicleId)
        assertEquals(ServiceOrderStatus.RECEIVED, response.status)
        assertEquals(BigDecimal("120.00"), response.totalPrice)
        
        verify { serviceOrderGateway.save(any()) }
        verify { notifyCustomer.execute(any(), ServiceOrderStatus.RECEIVED) }
    }

    @Test
    fun `should throw error when customer not found`() {
        val request = CreateServiceOrderRequest(
            customerEmail = "test@example.com",
            vehiclePlate = "ABC-1234",
            services = emptyList()
        )

        every { customerGateway.findByEmail(any()) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            createServiceOrderImpl.execute(request)
        }
    }

    @Test
    fun `should throw error when vehicle not found`() {
        val request = CreateServiceOrderRequest(
            customerEmail = "test@example.com",
            vehiclePlate = "ABC-1234",
            services = emptyList()
        )

        val customer = Customer(name = "John", document = Document("12345678909"))
        every { customerGateway.findByEmail(any()) } returns customer
        every { vehicleGateway.findByPlate(any()) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            createServiceOrderImpl.execute(request)
        }
    }
}
