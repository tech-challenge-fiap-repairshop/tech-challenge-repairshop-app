package com.cao.repairshop.execution.controller

import com.cao.repairshop.execution.dto.*
import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.serviceorder.service.ServiceOrderService

import com.cao.repairshop.core.exception.GlobalExceptionHandler
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.data.web.config.SpringDataJackson3Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class ExecutionControllerTest {

    private val serviceOrderService: ServiceOrderService = mockk()
    private lateinit var mockMvc: MockMvc
    private lateinit var mapper: JsonMapper

    private val executionId = UUID.randomUUID()
    private val serviceOrderId = UUID.randomUUID()
    private val basePath = "/service-orders/$serviceOrderId/executions"

    private val sampleResponse = ExecutionResponse(
        id = executionId,
        serviceOrderId = serviceOrderId,
        basicDescription = BasicExecution.OIL_CHANGE,
        fullDescription = "Oil change",
        price = BigDecimal("150.00"),
        estimatedTime = null,
        status = ExecutionStatus.INITIATED,
        created = LocalDateTime.now(),
        updated = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        mapper = JsonMapper.builder()
            .addModule(SpringDataJackson3Configuration.PageModule(null))
            .build()

        mockMvc = MockMvcBuilders
            .standaloneSetup(ExecutionController(serviceOrderService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(JacksonJsonHttpMessageConverter(mapper))
            .build()
    }

    @Test
    fun `POST executions should return 201`() {
        val request = CreateExecutionRequest(
            serviceOrderId = serviceOrderId,
            basicDescription = BasicExecution.OIL_CHANGE,
            fullDescription = "Oil change",
            price = BigDecimal("150.00")
        )
        every { serviceOrderService.addExecution(serviceOrderId, any()) } returns sampleResponse

        mockMvc.perform(
            post(basePath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.basicDescription").value("OIL_CHANGE"))
            .andExpect(jsonPath("$.status").value("INITIATED"))
    }

    @Test
    fun `GET execution by id should return 200`() {
        every { serviceOrderService.findExecution(serviceOrderId, executionId) } returns sampleResponse

        mockMvc.perform(get("$basePath/$executionId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(executionId.toString()))
    }

    @Test
    fun `PUT execution should return 200`() {
        val updateRequest = UpdateExecutionRequest(
            basicDescription = BasicExecution.SUSPENSION_REPLACEMENT,
            fullDescription = "Full service",
            price = BigDecimal("300.00")
        )
        every { serviceOrderService.updateExecution(serviceOrderId, executionId, any()) } returns sampleResponse.copy(
            basicDescription = BasicExecution.SUSPENSION_REPLACEMENT,
            fullDescription = "Full service"
        )

        mockMvc.perform(
            put("$basePath/$executionId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.basicDescription").value("SUSPENSION_REPLACEMENT"))
    }

    @Test
    fun `DELETE execution should return 204`() {
        every { serviceOrderService.removeExecution(serviceOrderId, executionId) } returns Unit

        mockMvc.perform(delete("$basePath/$executionId"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `PATCH execution status should return 200`() {
        val statusRequest = ExecutionStatusUpdateRequest(status = ExecutionStatus.PENDING)
        every { serviceOrderService.advanceExecutionStatus(serviceOrderId, executionId, ExecutionStatus.PENDING) } returns
            sampleResponse.copy(status = ExecutionStatus.PENDING)

        mockMvc.perform(
            patch("$basePath/$executionId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(statusRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PENDING"))
    }
}
