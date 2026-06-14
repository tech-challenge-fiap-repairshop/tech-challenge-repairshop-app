package com.cao.repairshop.serviceorder.controller

import com.cao.repairshop.core.exception.GlobalExceptionHandler
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.infra.controller.dtos.*
import com.cao.repairshop.serviceorder.infra.controller.ServiceOrderController
import com.cao.repairshop.serviceorder.application.usecases.*
import com.cao.repairshop.execution.infra.controller.dtos.ExecutionMetricsResponse
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.data.web.config.SpringDataJackson3Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class ServiceOrderControllerTest {

    private val createServiceOrder: CreateServiceOrder = mockk()
    private val findServiceOrder: FindServiceOrder = mockk()
    private val advanceServiceOrderStatus: AdvanceServiceOrderStatus = mockk()
    private val approveServiceOrder: ApproveServiceOrder = mockk()
    private val getServiceOrderMetrics: GetServiceOrderMetrics = mockk()
    private val getExecutionMetrics: GetExecutionMetrics = mockk()

    private lateinit var mockMvc: MockMvc
    private lateinit var mapper: JsonMapper

    private val orderId = UUID.randomUUID()
    private val customerId = UUID.randomUUID()
    private val vehicleId = UUID.randomUUID()
    private val defaultEmail = "customer@example.com"
    private val defaultPlate = "ABC-1234"

    private fun sampleResponse(status: ServiceOrderStatus = ServiceOrderStatus.RECEIVED): ServiceOrderResponse {
        return ServiceOrderResponse(
            id = orderId,
            customerId = customerId,
            vehicleId = vehicleId,
            status = status,
            totalPrice = BigDecimal("400.00"),
            enterTime = LocalDateTime.now(),
            endTime = null,
            created = LocalDateTime.now(),
            updated = LocalDateTime.now(),
            services = emptyList(),
            history = emptyList()
        )
    }

    @BeforeEach
    fun setUp() {
        mapper = JsonMapper.builder()
            .addModule(SpringDataJackson3Configuration.PageModule(null))
            .build()

        mockMvc = MockMvcBuilders
            .standaloneSetup(
                ServiceOrderController(
                    createServiceOrder,
                    findServiceOrder,
                    advanceServiceOrderStatus,
                    approveServiceOrder,
                    getServiceOrderMetrics,
                    getExecutionMetrics
                )
            )
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
            services = listOf(ExecutionDefinitionRequest(basicDescription = "OIL_CHANGE", price = BigDecimal("100.00")))
        )
        every { createServiceOrder.execute(any()) } returns sampleResponse()

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
        every { findServiceOrder.findAll(any<Pageable>()) } returns PageImpl(listOf(sampleResponse()))

        mockMvc.perform(get("/service-orders"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].status").value("RECEIVED"))
    }

    @Test
    fun `GET service-orders by id should return 200`() {
        every { findServiceOrder.findById(orderId) } returns sampleResponse()

        mockMvc.perform(get("/service-orders/$orderId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(orderId.toString()))
    }

    @Test
    fun `PATCH service-orders status should return 200`() {
        val statusRequest = ServiceOrderStatusUpdateRequest(status = ServiceOrderStatus.IN_DIAGNOSIS)
        every { advanceServiceOrderStatus.execute(orderId, ServiceOrderStatus.IN_DIAGNOSIS) } returns sampleResponse(ServiceOrderStatus.IN_DIAGNOSIS)

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
        every { approveServiceOrder.execute(orderId, any()) } returns sampleResponse(ServiceOrderStatus.APPROVED)

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
        every { getServiceOrderMetrics.execute() } returns metrics

        mockMvc.perform(get("/service-orders/metrics"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.averageExecutionTimeMinutes").value(120.0))
    }

    @Test
    fun `GET execution metrics should return 200`() {
        val metrics = ExecutionMetricsResponse(averageExecutionTimeMinutes = 45.0)
        every { getExecutionMetrics.execute() } returns metrics

        mockMvc.perform(get("/service-orders/executions/metrics"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.averageExecutionTimeMinutes").value(45.0))
    }
}
