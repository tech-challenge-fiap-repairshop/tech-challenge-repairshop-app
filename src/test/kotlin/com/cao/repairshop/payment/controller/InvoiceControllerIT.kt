package com.cao.repairshop.payment.controller

import com.cao.repairshop.payment.dto.CreateInvoiceRequest
import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Email
import com.cao.repairshop.register.domain.Plate
import com.cao.repairshop.register.entity.Customer
import com.cao.repairshop.register.entity.Vehicle
import com.cao.repairshop.register.repository.CustomerRepository
import com.cao.repairshop.register.repository.VehicleRepository
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import com.cao.repairshop.serviceorder.repository.ServiceOrderRepository
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

    private lateinit var finalizedOrder: ServiceOrder

    @BeforeEach
    fun setUp() {
        mapper = JsonMapper.builder()
            .addModule(SpringDataJackson3Configuration.PageModule(null))
            .build()
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
        val customer = customerRepository.save(
            Customer(name = "Invoice IT Customer", document = Document("52998224725"), email = Email("invoice@it.com"))
        )
        val vehicle = vehicleRepository.save(
            Vehicle(customer = customer, plate = Plate("INV1234"), brand = "Inv", model = "Integration")
        )
        finalizedOrder = serviceOrderRepository.save(
            ServiceOrder(
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
