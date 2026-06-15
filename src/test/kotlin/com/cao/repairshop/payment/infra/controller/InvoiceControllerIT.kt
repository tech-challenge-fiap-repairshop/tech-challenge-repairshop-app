package com.cao.repairshop.payment.infra.controller

import com.cao.repairshop.payment.infra.controller.dtos.CreateInvoiceRequest
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.config.SpringDataJackson3Configuration
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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
class InvoiceControllerIT {

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

    private lateinit var finalizedOrder: ServiceOrderEntity

    @BeforeEach
    fun setUp() {
        mapper = JsonMapper.builder()
            .addModule(SpringDataJackson3Configuration.PageModule(null))
            .build()
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
        val customer = customerRepository.save(
            CustomerEntity(name = "Invoice IT Customer", document = Document("52998224725"), email = Email("invoice@it.com"))
        )
        val vehicle = vehicleRepository.save(
            VehicleEntity(customer = customer, plate = Plate("INV1234"), brand = "Inv", model = "Integration")
        )
        finalizedOrder = serviceOrderRepository.save(
            ServiceOrderEntity(
                customer = customer, 
                vehicle = vehicle, 
                status = ServiceOrderStatus.FINALIZED,
                totalPrice = BigDecimal("1500.00")
            )
        )
    }

    @Test
    @WithMockUser(roles = ["ATTENDANT"])
    fun `should create and retrieve invoice`() {
        val createRequest = CreateInvoiceRequest(
            serviceOrderId = finalizedOrder.id,
            invoiceNumber = "NF-INTEGRATION-001"
        )

        // 1. Create Invoice
        val createResult = mockMvc.perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.invoiceNumber").value("NF-INTEGRATION-001"))
            .andExpect(jsonPath("$.price").value(1500.0))
            .andReturn()

        val invoiceId = UUID.fromString(mapper.readTree(createResult.response.contentAsString).get("id").asText())

        // 2. Get Invoice by ID
        mockMvc.perform(get("/invoices/$invoiceId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(invoiceId.toString()))

        // 3. Verify SO status updated to PAID
        mockMvc.perform(get("/service-orders/${finalizedOrder.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PAID"))
    }
}
