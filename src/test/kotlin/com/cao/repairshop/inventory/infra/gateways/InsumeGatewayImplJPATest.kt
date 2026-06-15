package com.cao.repairshop.inventory.infra.gateways

import com.cao.repairshop.inventory.domain.entities.Insume
import com.cao.repairshop.inventory.infra.persistence.models.InsumeEntity
import com.cao.repairshop.inventory.infra.persistence.repositories.InsumeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

class InsumeGatewayImplJPATest {

    private lateinit var insumeRepository: InsumeRepository
    private lateinit var insumeGatewayImplJPA: InsumeGatewayImplJPA

    @BeforeEach
    fun setup() {
        insumeRepository = mockk()
        insumeGatewayImplJPA = InsumeGatewayImplJPA(insumeRepository)
    }

    @Test
    fun `should save insume successfully`() {
        val insume = Insume(id = UUID.randomUUID(), name = "Oil", quantity = 10, price = BigDecimal.TEN, unityPrice = BigDecimal.TEN)
        val insumeEntity = InsumeEntity(id = insume.id, name = "Oil", quantity = 10, price = BigDecimal.TEN, unityPrice = BigDecimal.TEN)
        
        every { insumeRepository.save(any()) } returns insumeEntity

        val result = insumeGatewayImplJPA.save(insume)
        assertEquals("Oil", result.name)
        verify { insumeRepository.save(any()) }
    }

    @Test
    fun `should find by id successfully`() {
        val id = UUID.randomUUID()
        val insumeEntity = InsumeEntity(id = id, name = "Oil", quantity = 10, price = BigDecimal.TEN, unityPrice = BigDecimal.TEN)
        every { insumeRepository.findById(id) } returns Optional.of(insumeEntity)

        val result = insumeGatewayImplJPA.findById(id)
        assertNotNull(result)
        assertEquals("Oil", result?.name)
    }

    @Test
    fun `should find all successfully`() {
        val pageable = PageRequest.of(0, 10)
        val insumeEntity = InsumeEntity(id = UUID.randomUUID(), name = "Oil", quantity = 10, price = BigDecimal.TEN, unityPrice = BigDecimal.TEN)
        every { insumeRepository.findAll(pageable) } returns PageImpl(listOf(insumeEntity))

        val result = insumeGatewayImplJPA.findAll(pageable)
        assertEquals(1, result.totalElements)
    }

    @Test
    fun `should delete insume successfully`() {
        val insume = Insume(id = UUID.randomUUID(), name = "Oil", quantity = 10, price = BigDecimal.TEN, unityPrice = BigDecimal.TEN)
        every { insumeRepository.delete(any()) } returns Unit

        insumeGatewayImplJPA.delete(insume)
        verify { insumeRepository.delete(any()) }
    }
}
