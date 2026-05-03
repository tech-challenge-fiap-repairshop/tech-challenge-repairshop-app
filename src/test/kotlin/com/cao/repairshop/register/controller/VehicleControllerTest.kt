package com.cao.repairshop.register.controller

import com.cao.repairshop.core.exception.GlobalExceptionHandler
import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Plate
import com.cao.repairshop.register.dto.CreateVehicleRequest
import com.cao.repairshop.register.dto.UpdateVehicleRequest
import com.cao.repairshop.register.entity.Customer
import com.cao.repairshop.register.entity.Vehicle
import com.cao.repairshop.register.service.VehicleService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.json.JsonMapper
import java.util.UUID

class VehicleControllerTest {

    private val vehicleService: VehicleService = mockk()
    private lateinit var mockMvc: MockMvc
    private lateinit var mapper: JsonMapper

    private val vehicleId = UUID.randomUUID()
    private val customerId = UUID.randomUUID()

    private fun sampleVehicle(): Vehicle {
        val customer = Customer(id = customerId, name = "Owner", document = Document("52998224725"))
        return Vehicle(
            id = vehicleId,
            customer = customer,
            plate = Plate("ABC1234"),
            brand = "Toyota",
            model = "Corolla"
        )
    }

    @BeforeEach
    fun setUp() {
        mapper = JsonMapper.builder()
            .addModule(SpringDataJackson3Configuration.PageModule(null))
            .build()

        mockMvc = MockMvcBuilders
            .standaloneSetup(VehicleController(vehicleService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(JacksonJsonHttpMessageConverter(mapper))
            .build()
    }

    @Test
    fun `POST vehicles should return 201`() {
        val request = CreateVehicleRequest(
            customerEmail = "customer@example.com",
            plate = "ABC1234",
            brand = "Toyota",
            model = "Corolla"
        )
        every { vehicleService.create(any()) } returns sampleVehicle()

        mockMvc.perform(
            post("/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.plate").value("ABC1234"))
            .andExpect(jsonPath("$.brand").value("Toyota"))
    }

    @Test
    fun `GET vehicles should return 200 paginated`() {
        every { vehicleService.findAll(any()) } returns PageImpl(listOf(sampleVehicle()))

        mockMvc.perform(get("/vehicles"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].plate").value("ABC1234"))
    }

    @Test
    fun `GET vehicles by id should return 200`() {
        every { vehicleService.findById(vehicleId) } returns sampleVehicle()

        mockMvc.perform(get("/vehicles/$vehicleId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(vehicleId.toString()))
    }

    @Test
    fun `PUT vehicles by id should return 200`() {
        val updateRequest = UpdateVehicleRequest(plate = "XYZ9999", brand = "Honda", model = "Civic")
        val updatedVehicle = sampleVehicle().apply {
            plate = Plate("XYZ9999")
            brand = "Honda"
            model = "Civic"
        }
        every { vehicleService.update(vehicleId, any()) } returns updatedVehicle

        mockMvc.perform(
            put("/vehicles/$vehicleId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.brand").value("Honda"))
    }

    @Test
    fun `DELETE vehicles by id should return 204`() {
        every { vehicleService.delete(vehicleId) } returns Unit

        mockMvc.perform(delete("/vehicles/$vehicleId"))
            .andExpect(status().isNoContent)
    }
}
