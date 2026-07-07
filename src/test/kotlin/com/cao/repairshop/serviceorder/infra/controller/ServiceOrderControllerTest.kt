package com.cao.repairshop.serviceorder.infra.controller

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.GlobalExceptionHandler
import com.cao.repairshop.core.exception.InvalidStateTransitionException
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.infra.controller.dtos.*
import com.cao.repairshop.serviceorder.application.usecases.*
import com.cao.repairshop.execution.infra.controller.dtos.ExecutionMetricsResponse
import com.cao.repairshop.register.infra.persistence.models.CustomerEntity
import com.cao.repairshop.register.infra.persistence.models.VehicleEntity
import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import jakarta.persistence.criteria.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.hamcrest.CoreMatchers.containsString
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.data.jpa.domain.Specification

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
    fun `POST service-orders with invalid email should return 400`() {
        val request = CreateServiceOrderRequest(
            customerEmail = "invalid-email",
            vehiclePlate = defaultPlate,
            services = listOf(ExecutionDefinitionRequest(basicDescription = "OIL_CHANGE", price = BigDecimal("100.00")))
        )

        mockMvc.perform(
            post("/service-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET service-orders should return 200`() {
        val spec = slot<Specification<ServiceOrderEntity>>()
        every { findServiceOrder.findAll(capture(spec), any<Pageable>()) } returns PageImpl(listOf(sampleResponse()))

        mockMvc.perform(get("/service-orders"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].status").value("RECEIVED"))
    }

    @Test
    fun `GET service-orders with all filters should return 200`() {
        val spec = slot<Specification<ServiceOrderEntity>>()
        every { findServiceOrder.findAll(capture(spec), any<Pageable>()) } returns PageImpl(listOf(sampleResponse()))

        mockMvc.perform(get("/service-orders")
            .param("customerId", customerId.toString())
            .param("vehicleId", vehicleId.toString())
            .param("status", ServiceOrderStatus.RECEIVED.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].status").value("RECEIVED"))
    }

    @Test
    fun `GET service-orders with no parameters should build default status specification`() {
        val specSlot = slot<Specification<ServiceOrderEntity>>()
        every { findServiceOrder.findAll(capture(specSlot), any<Pageable>()) } returns PageImpl(listOf(sampleResponse()))

        mockMvc.perform(get("/service-orders"))
            .andExpect(status().isOk)

        val spec = specSlot.captured

        val root = mockk<Root<ServiceOrderEntity>>()
        val query = mockk<CriteriaQuery<*>>()
        val cb = mockk<CriteriaBuilder>()

        val statusPath = mockk<Path<ServiceOrderStatus>>()
        val inExpression = mockk<CriteriaBuilder.In<ServiceOrderStatus>>()
        val notPredicate = mockk<Predicate>()
        val andPredicate = mockk<Predicate>()

        every { root.get<ServiceOrderStatus>("status") } returns statusPath
        every {
            statusPath.`in`(
                ServiceOrderStatus.FINALIZED,
                ServiceOrderStatus.PAID,
                ServiceOrderStatus.CANCELED
            )
        } returns inExpression
        every { cb.not(inExpression) } returns notPredicate
        every { cb.and(notPredicate) } returns andPredicate

        val result = spec.toPredicate(root, query, cb)

        assertNotNull(result)
        assertSame(andPredicate, result)
    }

    @Test
    fun `GET service-orders with all filters should build specification with customer, vehicle and status predicates`() {
        val specSlot = slot<Specification<ServiceOrderEntity>>()
        every { findServiceOrder.findAll(capture(specSlot), any<Pageable>()) } returns PageImpl(listOf(sampleResponse()))

        mockMvc.perform(get("/service-orders")
            .param("customerId", customerId.toString())
            .param("vehicleId", vehicleId.toString())
            .param("status", ServiceOrderStatus.RECEIVED.toString()))
            .andExpect(status().isOk)

        val spec = specSlot.captured

        val root = mockk<Root<ServiceOrderEntity>>()
        val query = mockk<CriteriaQuery<*>>()
        val cb = mockk<CriteriaBuilder>()

        val customerPath = mockk<Path<CustomerEntity>>()
        val customerIdPath = mockk<Path<UUID>>()
        val vehiclePath = mockk<Path<VehicleEntity>>()
        val vehicleIdPath = mockk<Path<UUID>>()
        val statusPath = mockk<Path<ServiceOrderStatus>>()

        every { root.get<CustomerEntity>("customer") } returns customerPath
        every { customerPath.get<UUID>("id") } returns customerIdPath

        every { root.get<VehicleEntity>("vehicle") } returns vehiclePath
        every { vehiclePath.get<UUID>("id") } returns vehicleIdPath

        every { root.get<ServiceOrderStatus>("status") } returns statusPath

        val eqCustomer = mockk<Predicate>()
        val eqVehicle = mockk<Predicate>()
        val eqStatus = mockk<Predicate>()
        val andPredicate = mockk<Predicate>()

        every { cb.equal(customerIdPath, customerId) } returns eqCustomer
        every { cb.equal(vehicleIdPath, vehicleId) } returns eqVehicle
        every { cb.equal(statusPath, ServiceOrderStatus.RECEIVED) } returns eqStatus
        every { cb.and(eqCustomer, eqVehicle, eqStatus) } returns andPredicate

        val result = spec.toPredicate(root, query, cb)

        assertNotNull(result)
        assertSame(andPredicate, result)
    }

    @Test
    fun `GET service-orders by id should return 200`() {
        every { findServiceOrder.findById(orderId) } returns sampleResponse()

        mockMvc.perform(get("/service-orders/$orderId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(orderId.toString()))
    }

    @Test
    fun `GET service-orders by id should return 404 when not found`() {
        every { findServiceOrder.findById(orderId) } throws EntityNotFoundException("Service order not found")

        mockMvc.perform(get("/service-orders/$orderId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Resource not found"))
            .andExpect(jsonPath("$.message").value("Service order not found"))
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
    fun `PATCH service-orders status should return 422 when invalid status transition`() {
        val statusRequest = ServiceOrderStatusUpdateRequest(status = ServiceOrderStatus.RECEIVED)
        every { advanceServiceOrderStatus.execute(orderId, ServiceOrderStatus.RECEIVED) } throws InvalidStateTransitionException("Cannot transition back to RECEIVED")

        mockMvc.perform(
            patch("/service-orders/$orderId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(statusRequest))
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.error").value("Invalid state transition"))
            .andExpect(jsonPath("$.message").value("Cannot transition back to RECEIVED"))
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
