package com.cao.repairshop.inventory.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.inventory.application.gateways.InsumeGateway
import com.cao.repairshop.inventory.domain.entities.Insume
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class DeleteInsumeImplTest {

    private lateinit var insumeGateway: InsumeGateway
    private lateinit var deleteInsumeImpl: DeleteInsumeImpl

    @BeforeEach
    fun setup() {
        insumeGateway = mockk()
        deleteInsumeImpl = DeleteInsumeImpl(insumeGateway)
    }

    @Test
    fun `should delete insume successfully`() {
        val insumeId = UUID.randomUUID()
        val insume = Insume(id = insumeId, name = "Oil", price = BigDecimal.TEN, unityPrice = BigDecimal.TEN)

        every { insumeGateway.findById(insumeId) } returns insume
        every { insumeGateway.delete(insume) } returns Unit

        deleteInsumeImpl.execute(insumeId)

        verify { insumeGateway.delete(insume) }
    }

    @Test
    fun `should throw error when deleting and insume not found`() {
        every { insumeGateway.findById(any()) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            deleteInsumeImpl.execute(UUID.randomUUID())
        }
    }
}
