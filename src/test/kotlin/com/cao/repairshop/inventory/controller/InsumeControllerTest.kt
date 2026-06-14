package com.cao.repairshop.inventory.controller

import com.cao.repairshop.core.exception.GlobalExceptionHandler
import com.cao.repairshop.inventory.infra.controller.dtos.CreateInsumeRequest
import com.cao.repairshop.inventory.infra.controller.dtos.InsumeResponse
import com.cao.repairshop.inventory.infra.controller.dtos.UpdateInsumeRequest
import com.cao.repairshop.inventory.infra.controller.InsumeController
import com.cao.repairshop.inventory.application.usecases.CreateInsume
import com.cao.repairshop.inventory.application.usecases.FindInsume
import com.cao.repairshop.inventory.application.usecases.UpdateInsume
import com.cao.repairshop.inventory.application.usecases.DeleteInsume
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
import java.util.UUID

class InsumeControllerTest {

    private val createInsume: CreateInsume = mockk()
    private val findInsume: FindInsume = mockk()
    private val updateInsume: UpdateInsume = mockk()
    private val deleteInsume: DeleteInsume = mockk()
    private lateinit var mockMvc: MockMvc
    private lateinit var mapper: JsonMapper

    private val insumeId = UUID.randomUUID()
    private val sampleResponse = InsumeResponse(
        id = insumeId,
        name = "Oil Filter",
        brand = "Bosch",
        skuId = "SKU-001",
        quantity = 10,
        price = BigDecimal("50.00"),
        unityPrice = BigDecimal("5.00")
    )

    @BeforeEach
    fun setUp() {
        mapper = JsonMapper.builder()
            .addModule(SpringDataJackson3Configuration.PageModule(null))
            .build()

        mockMvc = MockMvcBuilders
            .standaloneSetup(InsumeController(createInsume, findInsume, updateInsume, deleteInsume))
            .setControllerAdvice(GlobalExceptionHandler())
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(JacksonJsonHttpMessageConverter(mapper))
            .build()
    }

    @Test
    fun `POST insumes should return 201`() {
        val request = CreateInsumeRequest(
            name = "Oil Filter",
            brand = "Bosch",
            quantity = 10,
            price = BigDecimal("50.00"),
            unityPrice = BigDecimal("5.00")
        )
        every { createInsume.execute(any()) } returns sampleResponse

        mockMvc.perform(
            post("/insumes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Oil Filter"))
            .andExpect(jsonPath("$.brand").value("Bosch"))
    }

    @Test
    fun `GET insumes should return 200 paginated`() {
        every { findInsume.findAll(any<Pageable>()) } returns PageImpl(listOf(sampleResponse))

        mockMvc.perform(get("/insumes"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].name").value("Oil Filter"))
    }

    @Test
    fun `GET insumes by id should return 200`() {
        every { findInsume.findById(insumeId) } returns sampleResponse

        mockMvc.perform(get("/insumes/$insumeId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(insumeId.toString()))
    }

    @Test
    fun `PUT insumes by id should return 200`() {
        val updateRequest = UpdateInsumeRequest(
            name = "Air Filter",
            quantity = 5,
            price = BigDecimal("30.00"),
            unityPrice = BigDecimal("6.00")
        )
        every { updateInsume.execute(insumeId, any()) } returns sampleResponse.copy(name = "Air Filter")

        mockMvc.perform(
            put("/insumes/$insumeId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Air Filter"))
    }

    @Test
    fun `DELETE insumes by id should return 204`() {
        every { deleteInsume.execute(insumeId) } returns Unit

        mockMvc.perform(delete("/insumes/$insumeId"))
            .andExpect(status().isNoContent)
    }
}
