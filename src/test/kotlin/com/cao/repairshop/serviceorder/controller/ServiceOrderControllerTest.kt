package com.cao.repairshop.serviceorder.controller

import com.cao.repairshop.serviceorder.service.ServiceOrderMetricsService
import com.cao.repairshop.serviceorder.service.ServiceOrderService
import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.serviceorder.dto.*
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import com.cao.repairshop.register.entity.Customer
import com.cao.repairshop.register.entity.Vehicle
import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Plate
import com.cao.repairshop.register.domain.Email

import com.cao.repairshop.core.exception.GlobalExceptionHandler
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class ServiceOrderControllerTest {

    private val serviceOrderService: ServiceOrderService = mockk()
    private val serviceOrderMetricsService: ServiceOrderMetricsService = mockk()
    private lateinit var mockMvc: MockMvc
    private lateinit var mapper: JsonMapper

    private val orderId = UUID.randomUUID()
    private val customerId = UUID.randomUUID()
    private val vehicleId = UUID.randomUUID()
    private val defaultEmail = "customer@example.com"
    private val defaultPlate = "ABC-1234"

    private fun sampleOrder(): ServiceOrder {
        val customer = Customer(id = customerId, name = "Owner", document = Document("52998224725"))
        val vehicle = Vehicle(id = vehicleId, customer = customer, plate = Plate(defaultPlate), brand = "T", model = "C")
        return ServiceOrder(
            id = orderId,
            customer = customer,
            vehicle = vehicle,
            status = ServiceOrderStatus.RECEIVED,
            totalPrice = BigDecimal("400.00")
        )
    }

    @BeforeEach
    fun setUp() {
        mapper = JsonMapper.builder()
            .addModule(SpringDataJackson3Configuration.PageModule(null))
            .build()

        mockMvc = MockMvcBuilders
            .standaloneSetup(ServiceOrderController(serviceOrderService, serviceOrderMetricsService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(JacksonJsonHttpMessageConverter(mapper))
            .build()
    }

    @Test
    fun `POST service-orders should return 201`() {
        val request = CreateServiceOrderRequest(
            customerEmail = defaultEmail,
            vehiclePlate = defaultPlate,
            services = listOf(ExecutionDefinitionRequest(basicDescription = BasicExecution.OIL_CHANGE, price = java.math.BigDecimal("100.00")))
        )
        every { serviceOrderService.createServiceOrder(any()) } returns sampleOrder()

        mockMvc.perform(
            post("/service-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("RECEIVED"))
            .andExpect(jsonPath("$.totalPrice").value(400.00))
    }

    @Test
    fun `GET service-orders should return 200`() {
        every { serviceOrderService.findAll(any()) } returns PageImpl(listOf(sampleOrder()))

        mockMvc.perform(get("/service-orders"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].status").value("RECEIVED"))
    }

    @Test
    fun `GET service-orders by id should return 200`() {
        every { serviceOrderService.findServiceOrder(orderId) } returns sampleOrder()

        mockMvc.perform(get("/service-orders/$orderId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(orderId.toString()))
    }

    @Test
    fun `PATCH service-orders status should return 200`() {
        val statusRequest = ServiceOrderStatusUpdateRequest(status = ServiceOrderStatus.IN_DIAGNOSIS)
        val updatedOrder = sampleOrder().apply { status = ServiceOrderStatus.IN_DIAGNOSIS }
        every { serviceOrderService.advanceStatus(orderId, ServiceOrderStatus.IN_DIAGNOSIS) } returns updatedOrder

        mockMvc.perform(
            patch("/service-orders/$orderId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(statusRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("IN_DIAGNOSIS"))
    }

    @Test
    fun `POST service-orders approve should return 200`() {
        val approvalRequest = ApprovalRequest(approved = true)
        val approvedOrder = sampleOrder().apply { status = ServiceOrderStatus.APPROVED }
        every { serviceOrderService.approve(orderId, any()) } returns approvedOrder

        mockMvc.perform(
            post("/service-orders/$orderId/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(approvalRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("APPROVED"))
    }

    @Test
    fun `GET service-orders metrics should return 200`() {
        val metrics = ServiceOrderMetricsResponse(averageExecutionTimeMinutes = 120.0)
        every { serviceOrderMetricsService.getMetrics() } returns metrics

        mockMvc.perform(get("/service-orders/metrics"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.averageExecutionTimeMinutes").value(120.0))
    }

    @Test
    fun `GET execution metrics should return 200`() {
        val metrics = com.cao.repairshop.execution.dto.ExecutionMetricsResponse(averageExecutionTimeMinutes = 45.0)
        every { serviceOrderMetricsService.getExecutionMetrics() } returns metrics

        mockMvc.perform(get("/service-orders/executions/metrics"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.averageExecutionTimeMinutes").value(45.0))
    }
}
