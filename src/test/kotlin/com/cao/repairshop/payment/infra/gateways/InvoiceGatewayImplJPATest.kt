package com.cao.repairshop.payment.infra.gateways

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.payment.domain.entities.Invoice
import com.cao.repairshop.payment.infra.persistence.models.InvoiceEntity
import com.cao.repairshop.payment.infra.persistence.repositories.InvoiceRepository
import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Document
import com.cao.repairshop.register.domain.entities.Email
import com.cao.repairshop.register.infra.persistence.models.CustomerEntity
import com.cao.repairshop.register.infra.persistence.repositories.CustomerRepository
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import com.cao.repairshop.serviceorder.infra.persistence.repositories.ServiceOrderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class InvoiceGatewayImplJPATest {

    private lateinit var invoiceRepository: InvoiceRepository
    private lateinit var serviceOrderRepository: ServiceOrderRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var invoiceGatewayImplJPA: InvoiceGatewayImplJPA

    @BeforeEach
    fun setup() {
        invoiceRepository = mockk()
        serviceOrderRepository = mockk()
        customerRepository = mockk()
        invoiceGatewayImplJPA = InvoiceGatewayImplJPA(invoiceRepository, serviceOrderRepository, customerRepository)
    }

    @Test
    fun `should save invoice successfully`() {
        val customerEntity = mockk<CustomerEntity>(relaxed = true)
        val serviceOrderEntity = mockk<ServiceOrderEntity>(relaxed = true)
        val invoiceEntity = InvoiceEntity(id = UUID.randomUUID(), customer = customerEntity, serviceOrder = serviceOrderEntity, price = BigDecimal.TEN, invoiceNumber = "INV-001", emissionDate = LocalDateTime.now())
        
        val customer = mockk<Customer>(relaxed = true)
        val serviceOrder = mockk<ServiceOrder>(relaxed = true)
        val invoice = Invoice(id = invoiceEntity.id, customer = customer, serviceOrder = serviceOrder, price = BigDecimal.TEN, invoiceNumber = "INV-001", emissionDate = invoiceEntity.emissionDate)

        every { serviceOrderRepository.findById(any()) } returns Optional.of(serviceOrderEntity)
        every { customerRepository.findById(any()) } returns Optional.of(customerEntity)
        every { invoiceRepository.save(any()) } returns invoiceEntity

        val result = invoiceGatewayImplJPA.save(invoice)
        assertEquals("INV-001", result.invoiceNumber)
        verify { invoiceRepository.save(any()) }
    }

    @Test
    fun `should throw EntityNotFoundException when saving invoice with invalid service order`() {
        val invoice = mockk<Invoice>(relaxed = true)
        every { serviceOrderRepository.findById(any()) } returns Optional.empty()

        assertThrows(EntityNotFoundException::class.java) {
            invoiceGatewayImplJPA.save(invoice)
        }
    }

    @Test
    fun `should throw EntityNotFoundException when saving invoice with invalid customer`() {
        val invoice = mockk<Invoice>(relaxed = true)
        every { serviceOrderRepository.findById(any()) } returns Optional.of(mockk(relaxed = true))
        every { customerRepository.findById(any()) } returns Optional.empty()

        assertThrows(EntityNotFoundException::class.java) {
            invoiceGatewayImplJPA.save(invoice)
        }
    }

    @Test
    fun `should find by id successfully`() {
        val id = UUID.randomUUID()
        val customerEntity = CustomerEntity(id = UUID.randomUUID(), name = "John", document = Document("12345678909"))
        val serviceOrderEntity = ServiceOrderEntity(id = UUID.randomUUID(), customer = customerEntity, vehicle = mockk(relaxed = true))
        val invoiceEntity = InvoiceEntity(id = id, customer = customerEntity, serviceOrder = serviceOrderEntity, price = BigDecimal.TEN, invoiceNumber = "INV-001", emissionDate = LocalDateTime.now())
        every { invoiceRepository.findDetailedById(id) } returns Optional.of(invoiceEntity)

        val result = invoiceGatewayImplJPA.findById(id)
        assertNotNull(result)
        assertEquals("INV-001", result?.invoiceNumber)
    }

    @Test
    fun `should find all successfully`() {
        val pageable = PageRequest.of(0, 10)
        val customerEntity = CustomerEntity(id = UUID.randomUUID(), name = "John", document = Document("12345678909"))
        val serviceOrderEntity = ServiceOrderEntity(id = UUID.randomUUID(), customer = customerEntity, vehicle = mockk(relaxed = true))
        val invoiceEntity = InvoiceEntity(id = UUID.randomUUID(), customer = customerEntity, serviceOrder = serviceOrderEntity, price = BigDecimal.TEN, invoiceNumber = "INV-001", emissionDate = LocalDateTime.now())
        every { invoiceRepository.findAll(pageable) } returns PageImpl(listOf(invoiceEntity))

        val result = invoiceGatewayImplJPA.findAll(pageable)
        assertEquals(1, result.totalElements)
    }

    @Test
    fun `should find by service order id successfully`() {
        val serviceOrderId = UUID.randomUUID()
        val customerEntity = CustomerEntity(id = UUID.randomUUID(), name = "John", document = Document("12345678909"))
        val serviceOrderEntity = ServiceOrderEntity(id = serviceOrderId, customer = customerEntity, vehicle = mockk(relaxed = true))
        val invoiceEntity = InvoiceEntity(id = UUID.randomUUID(), customer = customerEntity, serviceOrder = serviceOrderEntity, price = BigDecimal.TEN, invoiceNumber = "INV-001", emissionDate = LocalDateTime.now())
        every { invoiceRepository.findByServiceOrderId(serviceOrderId) } returns invoiceEntity

        val result = invoiceGatewayImplJPA.findByServiceOrderId(serviceOrderId)
        assertNotNull(result)
        assertEquals("INV-001", result?.invoiceNumber)
    }

    @Test
    fun `should find by invoice number successfully`() {
        val invoiceNumber = "INV-001"
        val customerEntity = CustomerEntity(id = UUID.randomUUID(), name = "John", document = Document("12345678909"))
        val serviceOrderEntity = ServiceOrderEntity(id = UUID.randomUUID(), customer = customerEntity, vehicle = mockk(relaxed = true))
        val invoiceEntity = InvoiceEntity(id = UUID.randomUUID(), customer = customerEntity, serviceOrder = serviceOrderEntity, price = BigDecimal.TEN, invoiceNumber = invoiceNumber, emissionDate = LocalDateTime.now())
        every { invoiceRepository.findByInvoiceNumber(invoiceNumber) } returns invoiceEntity

        val result = invoiceGatewayImplJPA.findByInvoiceNumber(invoiceNumber)
        assertNotNull(result)
        assertEquals(invoiceNumber, result?.invoiceNumber)
    }
}
