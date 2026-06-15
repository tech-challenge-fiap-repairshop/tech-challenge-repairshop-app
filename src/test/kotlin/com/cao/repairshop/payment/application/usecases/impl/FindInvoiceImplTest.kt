package com.cao.repairshop.payment.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.payment.application.gateways.InvoiceGateway
import com.cao.repairshop.payment.domain.entities.Invoice
import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Document
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.UUID

class FindInvoiceImplTest {

    private lateinit var invoiceGateway: InvoiceGateway
    private lateinit var findInvoiceImpl: FindInvoiceImpl

    @BeforeEach
    fun setup() {
        invoiceGateway = mockk()
        findInvoiceImpl = FindInvoiceImpl(invoiceGateway)
    }

    @Test
    fun `should find invoice by id successfully`() {
        val invoiceId = UUID.randomUUID()
        val customer = Customer(name = "John", document = Document("12345678909"))
        val serviceOrder = ServiceOrder(customerId = customer.id, vehicleId = UUID.randomUUID())
        
        val invoice = Invoice(
            id = invoiceId,
            customer = customer,
            serviceOrder = serviceOrder,
            price = BigDecimal("150.00"),
            invoiceNumber = "INV-001",
            vehiclePlate = "ABC-1234"
        )

        every { invoiceGateway.findById(invoiceId) } returns invoice

        val response = findInvoiceImpl.findById(invoiceId)

        assertEquals(invoiceId, response.id)
        assertEquals("INV-001", response.invoiceNumber)
    }

    @Test
    fun `should throw error when finding by id and invoice not found`() {
        every { invoiceGateway.findById(any()) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            findInvoiceImpl.findById(UUID.randomUUID())
        }
    }

    @Test
    fun `should find all invoices successfully`() {
        val customer = Customer(name = "John", document = Document("12345678909"))
        val serviceOrder = ServiceOrder(customerId = customer.id, vehicleId = UUID.randomUUID())
        
        val invoice = Invoice(
            customer = customer,
            serviceOrder = serviceOrder,
            price = BigDecimal("150.00"),
            invoiceNumber = "INV-001",
            vehiclePlate = "ABC-1234"
        )
        val pageable = PageRequest.of(0, 10)
        
        every { invoiceGateway.findAll(pageable) } returns PageImpl(listOf(invoice))

        val response = findInvoiceImpl.findAll(pageable)

        assertEquals(1, response.totalElements)
        assertEquals("INV-001", response.content[0].invoiceNumber)
    }
}
