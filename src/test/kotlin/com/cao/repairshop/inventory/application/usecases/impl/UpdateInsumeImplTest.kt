package com.cao.repairshop.inventory.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.inventory.application.gateways.InsumeGateway
import com.cao.repairshop.inventory.domain.entities.Insume
import com.cao.repairshop.inventory.infra.controller.dtos.UpdateInsumeRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class UpdateInsumeImplTest {

    private lateinit var insumeGateway: InsumeGateway
    private lateinit var updateInsumeImpl: UpdateInsumeImpl

    @BeforeEach
    fun setup() {
        insumeGateway = mockk()
        updateInsumeImpl = UpdateInsumeImpl(insumeGateway)
    }

    @Test
    fun `should update insume successfully`() {
        val insumeId = UUID.randomUUID()
        val request = UpdateInsumeRequest(
            name = "Oil Updated",
            price = BigDecimal("15.00"),
            unityPrice = BigDecimal("15.00"),
            quantity = 20
        )

        val insume = Insume(id = insumeId, name = "Oil", price = BigDecimal.TEN, unityPrice = BigDecimal.TEN, quantity = 10)

        every { insumeGateway.findById(insumeId) } returns insume
        every { insumeGateway.save(any()) } answers { firstArg() }

        val response = updateInsumeImpl.execute(insumeId, request)

        assertEquals("Oil Updated", response.name)
        assertEquals(BigDecimal("15.00"), response.price)
        assertEquals(BigDecimal("15.00"), response.unityPrice)
        assertEquals(20, response.quantity)
        
        verify { insumeGateway.save(insume) }
    }

    @Test
    fun `should throw error when insume not found`() {
        every { insumeGateway.findById(any()) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            updateInsumeImpl.execute(UUID.randomUUID(), mockk(relaxed = true))
        }
    }
}
