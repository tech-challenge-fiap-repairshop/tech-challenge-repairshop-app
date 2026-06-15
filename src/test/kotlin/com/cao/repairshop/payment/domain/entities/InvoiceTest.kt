package com.cao.repairshop.payment.domain.entities

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Document
import com.cao.repairshop.register.domain.entities.Email
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class InvoiceTest {

    private fun createCustomer() = Customer(
        name = "John Doe",
        email = Email("john@example.com"),
        document = Document("12345678909")
    )

    private fun createServiceOrder(customer: Customer) = ServiceOrder(
        customerId = customer.id,
        vehicleId = UUID.randomUUID()
    )

    @Test
    fun `should create Invoice and verify equality`() {
        val customer = createCustomer()
        val serviceOrder = createServiceOrder(customer)
        
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()
        val invoice1 = Invoice(
            id = id,
            customer = customer,
            serviceOrder = serviceOrder,
            price = BigDecimal("100.00"),
            invoiceNumber = "INV-001",
            emissionDate = now,
            vehiclePlate = "ABC-1234",
            created = now,
            updated = now
        )

        val invoice2 = Invoice(
            id = id,
            customer = customer,
            serviceOrder = serviceOrder,
            price = BigDecimal("100.00"),
            invoiceNumber = "INV-001",
            emissionDate = now,
            vehiclePlate = "ABC-1234",
            created = now,
            updated = now
        )
        
        val invoiceDifferent = Invoice(
            customer = customer,
            serviceOrder = serviceOrder,
            price = BigDecimal("200.00"),
            invoiceNumber = "INV-002"
        )

        assertEquals(invoice1, invoice2)
        assertNotEquals(invoice1, invoiceDifferent)
        assertNotEquals(invoice1, null)
        assertNotEquals(invoice1, Any())
        
        assertEquals(invoice1.hashCode(), invoice2.hashCode())
        
        assertTrue(invoice1.toString().contains(id.toString()))
        assertTrue(invoice1.toString().contains("INV-001"))
    }
}
