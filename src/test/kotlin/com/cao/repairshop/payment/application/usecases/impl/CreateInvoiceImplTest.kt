package com.cao.repairshop.payment.application.usecases.impl

import com.cao.repairshop.core.exception.DuplicateEntityException
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.InvalidStateTransitionException
import com.cao.repairshop.payment.application.gateways.InvoiceGateway
import com.cao.repairshop.payment.domain.entities.Invoice
import com.cao.repairshop.payment.infra.controller.dtos.CreateInvoiceRequest
import com.cao.repairshop.register.application.gateways.CustomerGateway
import com.cao.repairshop.register.application.gateways.VehicleGateway
import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Document
import com.cao.repairshop.register.domain.entities.Plate
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class CreateInvoiceImplTest {

    private lateinit var invoiceGateway: InvoiceGateway
    private lateinit var serviceOrderGateway: ServiceOrderGateway
    private lateinit var customerGateway: CustomerGateway
    private lateinit var vehicleGateway: VehicleGateway
    private lateinit var createInvoiceImpl: CreateInvoiceImpl

    @BeforeEach
    fun setup() {
        invoiceGateway = mockk()
        serviceOrderGateway = mockk()
        customerGateway = mockk()
        vehicleGateway = mockk()
        createInvoiceImpl = CreateInvoiceImpl(
            invoiceGateway, serviceOrderGateway, customerGateway, vehicleGateway
        )
    }

    @Test
    fun `should create invoice successfully`() {
        val request = CreateInvoiceRequest(
            serviceOrderId = UUID.randomUUID(),
            invoiceNumber = "INV-001"
        )

        val customer = Customer(name = "John", document = Document("12345678909"))
        val vehicle = Vehicle(brand = "Ford", model = "Fiesta", plate = Plate("ABC-1234"), customerId = customer.id)
        val serviceOrder = ServiceOrder(
            id = request.serviceOrderId,
            customerId = customer.id,
            vehicleId = vehicle.id,
            status = ServiceOrderStatus.FINALIZED,
            totalPrice = BigDecimal("150.00"),
            enterTime = LocalDateTime.now()
        )

        every { serviceOrderGateway.findDetailedById(request.serviceOrderId) } returns serviceOrder
        every { invoiceGateway.findByServiceOrderId(request.serviceOrderId) } returns null
        every { invoiceGateway.findByInvoiceNumber(request.invoiceNumber) } returns null
        every { customerGateway.findById(customer.id) } returns customer
        every { vehicleGateway.findById(vehicle.id) } returns vehicle
        every { serviceOrderGateway.save(any()) } answers { firstArg() }
        every { invoiceGateway.save(any()) } answers { firstArg() }

        val response = createInvoiceImpl.execute(request)

        assertEquals("INV-001", response.invoiceNumber)
        assertEquals(BigDecimal("150.00"), response.price)
        assertEquals(ServiceOrderStatus.PAID, serviceOrder.status)
        
        verify { invoiceGateway.save(any()) }
        verify { serviceOrderGateway.save(serviceOrder) }
    }

    @Test
    fun `should throw error when order is not FINALIZED`() {
        val request = CreateInvoiceRequest(
            serviceOrderId = UUID.randomUUID(),
            invoiceNumber = "INV-001"
        )

        val serviceOrder = ServiceOrder(
            id = request.serviceOrderId,
            customerId = UUID.randomUUID(),
            vehicleId = UUID.randomUUID(),
            status = ServiceOrderStatus.IN_EXECUTION,
            enterTime = LocalDateTime.now()
        )

        every { serviceOrderGateway.findDetailedById(request.serviceOrderId) } returns serviceOrder

        assertThrows(InvalidStateTransitionException::class.java) {
            createInvoiceImpl.execute(request)
        }
    }

    @Test
    fun `should throw error when invoice number already exists`() {
        val request = CreateInvoiceRequest(
            serviceOrderId = UUID.randomUUID(),
            invoiceNumber = "INV-001"
        )

        val serviceOrder = ServiceOrder(
            id = request.serviceOrderId,
            customerId = UUID.randomUUID(),
            vehicleId = UUID.randomUUID(),
            status = ServiceOrderStatus.FINALIZED,
            enterTime = LocalDateTime.now()
        )

        every { serviceOrderGateway.findDetailedById(request.serviceOrderId) } returns serviceOrder
        every { invoiceGateway.findByServiceOrderId(request.serviceOrderId) } returns null
        every { invoiceGateway.findByInvoiceNumber(request.invoiceNumber) } returns mockk<Invoice>()

        assertThrows(DuplicateEntityException::class.java) {
            createInvoiceImpl.execute(request)
        }
    }
}
