package com.cao.repairshop.payment.controller

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.GlobalExceptionHandler
import com.cao.repairshop.payment.infra.controller.dtos.CreateInvoiceRequest
import com.cao.repairshop.payment.infra.controller.dtos.InvoiceResponse
import com.cao.repairshop.payment.infra.controller.InvoiceController
import com.cao.repairshop.payment.application.usecases.CreateInvoice
import com.cao.repairshop.payment.application.usecases.FindInvoice
import com.cao.repairshop.payment.application.usecases.FindAllInvoices
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.data.web.config.SpringDataJackson3Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class InvoiceControllerTest {

    private val createInvoice: CreateInvoice = mockk()
    private val findInvoice: FindInvoice = mockk()
    private val findAllInvoices: FindAllInvoices = mockk()
    private lateinit var mockMvc: MockMvc
    private lateinit var mapper: JsonMapper

    private val invoiceId = UUID.randomUUID()
    private val serviceOrderId = UUID.randomUUID()
    private val customerId = UUID.randomUUID()

    private fun buildInvoiceResponse(invoiceNumber: String = "NF-2026-001"): InvoiceResponse {
        return InvoiceResponse(
            id = invoiceId,
            customerId = customerId,
            customerName = "Test Customer",
            serviceOrderId = serviceOrderId,
            vehiclePlate = "ABC-1234",
            serviceOrderStatus = "FINALIZED",
            invoiceNumber = invoiceNumber,
            price = BigDecimal("500.00"),
            emissionDate = LocalDateTime.now(),
            items = emptyList(),
            created = LocalDateTime.now(),
            updated = LocalDateTime.now()
        )
    }

    @BeforeEach
    fun setUp() {
        mapper = JsonMapper.builder()
            .addModule(SpringDataJackson3Configuration.PageModule(null))
            .build()

        mockMvc = MockMvcBuilders
            .standaloneSetup(InvoiceController(createInvoice, findInvoice, findAllInvoices))
            .setControllerAdvice(GlobalExceptionHandler())
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(JacksonJsonHttpMessageConverter(mapper))
            .build()
    }

    @Test
    fun `POST invoices should return 201`() {
        val request = CreateInvoiceRequest(
            serviceOrderId = serviceOrderId,
            invoiceNumber = "NF-2026-001"
        )
        every { createInvoice.execute(any()) } returns buildInvoiceResponse()

        mockMvc.perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.invoiceNumber").value("NF-2026-001"))
            .andExpect(jsonPath("$.price").value(500.00))
            .andExpect(jsonPath("$.customerName").value("Test Customer"))
    }

    @Test
    fun `GET invoices paginated should return 200`() {
        every { findAllInvoices.execute(any()) } returns PageImpl(listOf(buildInvoiceResponse()))

        mockMvc.perform(get("/invoices"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].invoiceNumber").value("NF-2026-001"))
            .andExpect(jsonPath("$.content[0].customerName").value("Test Customer"))
            .andExpect(jsonPath("$.content[0].vehiclePlate").value("ABC-1234"))
    }

    @Test
    fun `GET invoices by id should return 200`() {
        every { findInvoice.execute(invoiceId) } returns buildInvoiceResponse()

        mockMvc.perform(get("/invoices/$invoiceId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(invoiceId.toString()))
            .andExpect(jsonPath("$.serviceOrderId").value(serviceOrderId.toString()))
            .andExpect(jsonPath("$.serviceOrderStatus").value("FINALIZED"))
    }

    @Test
    fun `GET invoices by id not found should return 404`() {
        every { findInvoice.execute(invoiceId) } throws EntityNotFoundException("Invoice not found")

        mockMvc.perform(get("/invoices/$invoiceId"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `POST invoices should return 201 with empty items list`() {
        val request = CreateInvoiceRequest(
            serviceOrderId = serviceOrderId,
            invoiceNumber = "NF-2026-002"
        )
        every { createInvoice.execute(any()) } returns buildInvoiceResponse("NF-2026-002")

        mockMvc.perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.items").isArray)
            .andExpect(jsonPath("$.items.length()").value(0))
    }
}
