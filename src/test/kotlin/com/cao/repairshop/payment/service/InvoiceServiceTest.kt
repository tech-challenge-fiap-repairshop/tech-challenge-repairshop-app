package com.cao.repairshop.payment.service

import com.cao.repairshop.core.exception.DuplicateEntityException
import com.cao.repairshop.core.exception.InvalidStateTransitionException
import com.cao.repairshop.payment.dto.CreateInvoiceRequest
import com.cao.repairshop.payment.entity.Invoice
import com.cao.repairshop.payment.repository.InvoiceRepository
import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Email
import com.cao.repairshop.register.domain.Plate
import com.cao.repairshop.register.entity.Customer
import com.cao.repairshop.register.entity.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import com.cao.repairshop.serviceorder.service.ServiceOrderService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class InvoiceServiceTest {

    @MockK
    private lateinit var invoiceRepository: InvoiceRepository

    @MockK
    private lateinit var serviceOrderService: ServiceOrderService

    @InjectMockKs
    private lateinit var invoiceService: InvoiceService

    private fun buildCustomer() =
        Customer(name = "Test Customer", document = Document("529.982.247-25"), email = Email("test@example.com"))

    private fun buildVehicle(customer: Customer) =
        Vehicle(customer = customer, plate = Plate("ABC-1234"), brand = "Toyota", model = "Corolla")

    private fun buildOrder(status: ServiceOrderStatus) =
        ServiceOrder(
            id = UUID.randomUUID(),
            customer = buildCustomer(),
            vehicle = buildVehicle(buildCustomer()),
            status = status,
            enterTime = LocalDateTime.now()
        )

    @Test
    fun `create invoice success - transitions SO to PAID`() {
        val order = buildOrder(ServiceOrderStatus.FINALIZED).apply { totalPrice = BigDecimal("1000.00") }
        val request = CreateInvoiceRequest(
            serviceOrderId = order.id,
            invoiceNumber = "NF-001"
        )

        every { serviceOrderService.findServiceOrderById(order.id) } returns order
        every { invoiceRepository.findByServiceOrderId(order.id) } returns null
        every { invoiceRepository.findByInvoiceNumber(request.invoiceNumber) } returns null
        every { invoiceRepository.save(any()) } answers { firstArg<Invoice>() }

        val result = invoiceService.create(request)

        assertThat(result).isNotNull
        assertThat(result.price).isEqualTo(BigDecimal("1000.00"))
        assertThat(order.status).isEqualTo(ServiceOrderStatus.PAID)
        verify { invoiceRepository.save(any()) }
    }

    @Test
    fun `create invoice fails when SO is not FINALIZED`() {
        val order = buildOrder(ServiceOrderStatus.IN_EXECUTION)
        val request = CreateInvoiceRequest(
            serviceOrderId = order.id,
            invoiceNumber = "NF-001"
        )

        every { serviceOrderService.findServiceOrderById(order.id) } returns order

        assertThatThrownBy { invoiceService.create(request) }
            .isInstanceOf(InvalidStateTransitionException::class.java)
            .hasMessageContaining("FINALIZED")
    }

    @Test
    fun `create invoice fails on duplicate invoice number`() {
        val order = buildOrder(ServiceOrderStatus.FINALIZED)
        val request = CreateInvoiceRequest(
            serviceOrderId = order.id,
            invoiceNumber = "NF-EXISTING"
        )
        val existingInvoice = mockk<Invoice>()

        every { serviceOrderService.findServiceOrderById(order.id) } returns order
        every { invoiceRepository.findByServiceOrderId(order.id) } returns null
        every { invoiceRepository.findByInvoiceNumber(request.invoiceNumber) } returns existingInvoice

        assertThatThrownBy { invoiceService.create(request) }
            .isInstanceOf(DuplicateEntityException::class.java)
    }

    @Test
    fun `findById success - returns Invoice`() {
        val id = UUID.randomUUID()
        val invoice = mockk<Invoice>()
        every { invoiceRepository.findDetailedById(id) } returns java.util.Optional.of(invoice)

        val result = invoiceService.findById(id)

        assertThat(result).isNotNull
    }

    @Test
    fun `findAll paginated - returns Page of Invoice`() {
        val pageable = org.springframework.data.domain.PageRequest.of(0, 10)
        val page = org.springframework.data.domain.PageImpl(listOf(mockk<Invoice>()))
        every { invoiceRepository.findAll(pageable) } returns page

        val result = invoiceService.findAll(pageable)

        assertThat(result.totalElements).isEqualTo(1)
    }
}
