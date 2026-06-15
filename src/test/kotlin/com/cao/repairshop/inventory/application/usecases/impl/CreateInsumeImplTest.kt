package com.cao.repairshop.inventory.application.usecases.impl

import com.cao.repairshop.inventory.application.gateways.InsumeGateway
import com.cao.repairshop.inventory.domain.entities.Insume
import com.cao.repairshop.inventory.infra.controller.dtos.CreateInsumeRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CreateInsumeImplTest {

    private lateinit var insumeGateway: InsumeGateway
    private lateinit var createInsumeImpl: CreateInsumeImpl

    @BeforeEach
    fun setup() {
        insumeGateway = mockk()
        createInsumeImpl = CreateInsumeImpl(insumeGateway)
    }

    @Test
    fun `should create insume successfully`() {
        val request = CreateInsumeRequest(
            name = "Oil",
            brand = "Mobil",
            quantity = 10,
            price = BigDecimal("15.00"),
            unityPrice = BigDecimal("15.00")
        )

        every { insumeGateway.save(any()) } answers { firstArg() }

        val result = createInsumeImpl.execute(request)

        assertEquals("Oil", result.name)
        assertEquals("Mobil", result.brand)
        assertEquals(10, result.quantity)

        verify { insumeGateway.save(any<Insume>()) }
    }
}
