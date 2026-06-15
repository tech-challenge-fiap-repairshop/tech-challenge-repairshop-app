package com.cao.repairshop.inventory.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.inventory.application.gateways.InsumeGateway
import com.cao.repairshop.inventory.domain.entities.Insume
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class DeductInsumeStockImplTest {

    private lateinit var insumeGateway: InsumeGateway
    private lateinit var deductInsumeStockImpl: DeductInsumeStockImpl

    @BeforeEach
    fun setup() {
        insumeGateway = mockk()
        deductInsumeStockImpl = DeductInsumeStockImpl(insumeGateway)
    }

    @Test
    fun `should deduct stock successfully`() {
        val insumeId = UUID.randomUUID()
        val insume = Insume(id = insumeId, name = "Oil", price = BigDecimal.TEN, unityPrice = BigDecimal.TEN, quantity = 10)

        every { insumeGateway.findById(insumeId) } returns insume
        every { insumeGateway.save(any()) } answers { firstArg() }

        deductInsumeStockImpl.execute(insumeId, 3)

        assertEquals(7, insume.quantity)
        verify { insumeGateway.save(insume) }
    }

    @Test
    fun `should throw error when insume not found`() {
        every { insumeGateway.findById(any()) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            deductInsumeStockImpl.execute(UUID.randomUUID(), 3)
        }
    }
}
