package com.cao.repairshop.inventory.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.inventory.application.gateways.InsumeGateway
import com.cao.repairshop.inventory.domain.entities.Insume
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.UUID

class FindInsumeImplTest {

    private lateinit var insumeGateway: InsumeGateway
    private lateinit var findInsumeImpl: FindInsumeImpl

    @BeforeEach
    fun setup() {
        insumeGateway = mockk()
        findInsumeImpl = FindInsumeImpl(insumeGateway)
    }

    @Test
    fun `should find insume by id successfully`() {
        val insumeId = UUID.randomUUID()
        val insume = Insume(id = insumeId, name = "Oil", price = BigDecimal.TEN, unityPrice = BigDecimal.TEN)

        every { insumeGateway.findById(insumeId) } returns insume

        val response = findInsumeImpl.findById(insumeId)

        assertEquals(insumeId, response.id)
        assertEquals("Oil", response.name)
    }

    @Test
    fun `should throw error when finding by id and insume not found`() {
        every { insumeGateway.findById(any()) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            findInsumeImpl.findById(UUID.randomUUID())
        }
    }

    @Test
    fun `should get entity by id successfully`() {
        val insumeId = UUID.randomUUID()
        val insume = Insume(id = insumeId, name = "Oil", price = BigDecimal.TEN, unityPrice = BigDecimal.TEN)

        every { insumeGateway.findById(insumeId) } returns insume

        val entity = findInsumeImpl.getEntityById(insumeId)

        assertEquals(insumeId, entity.id)
        assertEquals("Oil", entity.name)
    }

    @Test
    fun `should throw error when getting entity by id and insume not found`() {
        every { insumeGateway.findById(any()) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            findInsumeImpl.getEntityById(UUID.randomUUID())
        }
    }

    @Test
    fun `should find all insumes successfully`() {
        val insume = Insume(name = "Oil", price = BigDecimal.TEN, unityPrice = BigDecimal.TEN)
        val pageable = PageRequest.of(0, 10)
        
        every { insumeGateway.findAll(pageable) } returns PageImpl(listOf(insume))

        val response = findInsumeImpl.findAll(pageable)

        assertEquals(1, response.totalElements)
        assertEquals("Oil", response.content[0].name)
    }
}
