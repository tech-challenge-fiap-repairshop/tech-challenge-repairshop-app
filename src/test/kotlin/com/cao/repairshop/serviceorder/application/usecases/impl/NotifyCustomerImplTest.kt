package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.notification.EmailService
import com.cao.repairshop.core.notification.dto.EmailRequest
import com.cao.repairshop.register.application.gateways.CustomerGateway
import com.cao.repairshop.register.application.gateways.VehicleGateway
import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Document
import com.cao.repairshop.register.domain.entities.Email
import com.cao.repairshop.register.domain.entities.Plate
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class NotifyCustomerImplTest {

    private lateinit var customerGateway: CustomerGateway
    private lateinit var vehicleGateway: VehicleGateway
    private lateinit var emailService: EmailService
    private lateinit var notifyCustomerImpl: NotifyCustomerImpl

    @BeforeEach
    fun setup() {
        customerGateway = mockk()
        vehicleGateway = mockk()
        emailService = mockk(relaxed = true)
        notifyCustomerImpl = NotifyCustomerImpl(customerGateway, vehicleGateway, emailService)
    }

    @Test
    fun `should send notification email successfully`() {
        val customerId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val order = ServiceOrder(
            id = UUID.randomUUID(),
            customerId = customerId,
            vehicleId = vehicleId
        )

        val customer = Customer(
            id = customerId,
            name = "John Doe",
            document = Document("12345678909"),
            email = Email("john@example.com")
        )

        val vehicle = Vehicle(
            id = vehicleId,
            brand = "Ford",
            model = "Fiesta",
            plate = Plate("ABC-1234"),
            customerId = customerId
        )

        every { customerGateway.findById(customerId) } returns customer
        every { vehicleGateway.findById(vehicleId) } returns vehicle

        val emailRequestSlot = slot<EmailRequest>()
        every { emailService.sendEmail(capture(emailRequestSlot)) } returns Unit

        notifyCustomerImpl.execute(order, ServiceOrderStatus.RECEIVED)

        verify { emailService.sendEmail(any()) }
        val sentRequest = emailRequestSlot.captured
        assertEquals("john@example.com", sentRequest.to)
        assertEquals("Ordem de Serviço #${order.id} - Recebida", sentRequest.subject)
    }

    @Test
    fun `should throw EntityNotFoundException if customer is not found`() {
        val customerId = UUID.randomUUID()
        val order = ServiceOrder(
            id = UUID.randomUUID(),
            customerId = customerId,
            vehicleId = UUID.randomUUID()
        )

        every { customerGateway.findById(customerId) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            notifyCustomerImpl.execute(order, ServiceOrderStatus.RECEIVED)
        }
    }

    @Test
    fun `should throw EntityNotFoundException if vehicle is not found`() {
        val customerId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val order = ServiceOrder(
            id = UUID.randomUUID(),
            customerId = customerId,
            vehicleId = vehicleId
        )

        val customer = Customer(
            id = customerId,
            name = "John Doe",
            document = Document("12345678909"),
            email = Email("john@example.com")
        )

        every { customerGateway.findById(customerId) } returns customer
        every { vehicleGateway.findById(vehicleId) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            notifyCustomerImpl.execute(order, ServiceOrderStatus.RECEIVED)
        }
    }
}
