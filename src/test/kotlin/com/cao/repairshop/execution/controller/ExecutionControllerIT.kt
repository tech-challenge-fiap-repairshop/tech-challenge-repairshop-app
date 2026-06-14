package com.cao.repairshop.execution.controller

import com.cao.repairshop.execution.infra.controller.dtos.CreateExecutionRequest
import com.cao.repairshop.register.domain.entities.Document
import com.cao.repairshop.register.domain.entities.Email
import com.cao.repairshop.register.domain.entities.Plate
import com.cao.repairshop.register.infra.persistence.models.CustomerEntity
import com.cao.repairshop.register.infra.persistence.models.VehicleEntity
import com.cao.repairshop.register.infra.persistence.repositories.CustomerRepository
import com.cao.repairshop.register.infra.persistence.repositories.VehicleRepository
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import com.cao.repairshop.serviceorder.infra.persistence.repositories.ServiceOrderRepository
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.config.SpringDataJackson3Configuration
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import tools.jackson.databind.json.JsonMapper
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExecutionControllerIT {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mapper: JsonMapper

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var vehicleRepository: VehicleRepository

    @Autowired
    private lateinit var serviceOrderRepository: ServiceOrderRepository

    private lateinit var serviceOrder: ServiceOrderEntity

    @BeforeEach
    fun setUp() {
        mapper = JsonMapper.builder()
            .addModule(SpringDataJackson3Configuration.PageModule(null))
            .build()
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
        val customer = customerRepository.save(
            CustomerEntity(name = "Integration Test Customer", document = Document("52998224725"), email = Email("it@example.com"))
        )
        val vehicle = vehicleRepository.save(
            VehicleEntity(customer = customer, plate = Plate("ABC1234"), brand = "Test", model = "Integration")
        )
        serviceOrder = serviceOrderRepository.save(
            ServiceOrderEntity(customer = customer, vehicle = vehicle, status = ServiceOrderStatus.IN_EXECUTION)
        )
    }

    @Test
    @WithMockUser(roles = ["ATTENDANT"])
    fun `should create, list and update execution status in a real database environment`() {
        val createRequest = CreateExecutionRequest(
            serviceOrderId = serviceOrder.id,
            basicDescription = "OIL_CHANGE",
            fullDescription = "Detailed oil change",
            price = BigDecimal("150.00")
        )

        // 1. Create
        val createResult = mockMvc.perform(
            post("/service-orders/${serviceOrder.id}/executions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("INITIATED"))
            .andExpect(jsonPath("$.totalPrice").value(150.0))
            .andReturn()

        val executionId = UUID.fromString(mapper.readTree(createResult.response.contentAsString).get("id").asText())

        // 2. Get
        mockMvc.perform(get("/service-orders/${serviceOrder.id}/executions/$executionId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.basicDescription").value("OIL_CHANGE"))

        // 3. Advance Status to PENDING
        mockMvc.perform(
            patch("/service-orders/${serviceOrder.id}/executions/$executionId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"status": "PENDING"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PENDING"))

        // 4. Advance Status to FINALIZED
        mockMvc.perform(
            patch("/service-orders/${serviceOrder.id}/executions/$executionId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"status": "FINALIZED"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("FINALIZED"))

        // 5. Verify Service Order status update (it should auto-finalize if it was the only execution)
        mockMvc.perform(get("/service-orders/${serviceOrder.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("FINALIZED"))
    }

    @Test
    @WithMockUser(roles = ["ATTENDANT"])
    fun `should return 400 with dynamic message when basicDescription is invalid`() {
        val createRequest = mapOf(
            "serviceOrderId" to serviceOrder.id.toString(),
            "basicDescription" to "INVALID_SERVICE",
            "price" to 150.0
        )

        mockMvc.perform(
            post("/service-orders/${serviceOrder.id}/executions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value(containsString("basicDescription: Invalid service category. Valid values are: OIL_CHANGE, SUSPENSION_REPLACEMENT, WHEEL_ALIGNMENT, BRAKE_INSPECTION, ENGINE_DIAGNOSIS, OTHER")))
    }
}
