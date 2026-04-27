package com.cao.repairshop.inventory.service

import com.cao.repairshop.inventory.entity.Insume
import com.cao.repairshop.inventory.dto.*
import com.cao.repairshop.inventory.repository.InsumeRepository

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.InsufficientStockException
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
class InsumeServiceTest {

    @MockK
    private lateinit var insumeRepository: InsumeRepository

    @InjectMockKs
    private lateinit var insumeService: InsumeService

    private fun buildInsume(quantity: Int = 10) = Insume(
        name = "Brake pad",
        price = BigDecimal("50.00"),
        unityPrice = BigDecimal("30.00"),
        quantity = quantity
    )

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    fun `create success - returns InsumeResponse`() {
        val insume = buildInsume()
        val request = CreateInsumeRequest(
            name = "Brake pad",
            price = BigDecimal("50.00"),
            unityPrice = BigDecimal("30.00"),
            quantity = 10
        )

        every { insumeRepository.save(any()) } returns insume

        val result = insumeService.create(request)

        assertThat(result.name).isEqualTo("Brake pad")
        assertThat(result.quantity).isEqualTo(10)
        verify(exactly = 1) { insumeRepository.save(any()) }
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    fun `findById success - returns InsumeResponse`() {
        val insume = buildInsume()

        every { insumeRepository.findById(insume.id) } returns Optional.of(insume)

        val result = insumeService.findById(insume.id)

        assertThat(result.name).isEqualTo("Brake pad")
    }

    @Test
    fun `findById not found - throws EntityNotFoundException`() {
        val id = UUID.randomUUID()

        every { insumeRepository.findById(id) } returns Optional.empty()

        assertThatThrownBy { insumeService.findById(id) }
            .isInstanceOf(EntityNotFoundException::class.java)
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    fun `findAll paginated - returns Page of InsumeResponse`() {
        val pageable = PageRequest.of(0, 10)
        val insume = buildInsume()
        val page = PageImpl(listOf(insume), pageable, 1)

        every { insumeRepository.findAll(pageable) } returns page

        val result = insumeService.findAll(pageable)

        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content).hasSize(1)
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    fun `update success - returns updated InsumeResponse`() {
        val insume = buildInsume()
        val request = UpdateInsumeRequest(
            name = "Updated pad",
            price = BigDecimal("60.00"),
            unityPrice = BigDecimal("35.00"),
            quantity = 5
        )

        every { insumeRepository.findById(insume.id) } returns Optional.of(insume)
        every { insumeRepository.save(any()) } returns insume

        val result = insumeService.update(insume.id, request)

        assertThat(result).isNotNull
        verify(exactly = 1) { insumeRepository.save(any()) }
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    fun `delete success - deletes insume`() {
        val insume = buildInsume()

        every { insumeRepository.findById(insume.id) } returns Optional.of(insume)
        every { insumeRepository.delete(insume) } just runs

        insumeService.delete(insume.id)

        verify(exactly = 1) { insumeRepository.delete(insume) }
    }

    @Test
    fun `delete not found - throws EntityNotFoundException`() {
        val id = UUID.randomUUID()

        every { insumeRepository.findById(id) } returns Optional.empty()

        assertThatThrownBy { insumeService.delete(id) }
            .isInstanceOf(EntityNotFoundException::class.java)
    }

    // -------------------------------------------------------------------------
    // deductStock
    // -------------------------------------------------------------------------

    @Test
    fun `deductStock success - quantity decremented`() {
        val insume = buildInsume(quantity = 10)

        every { insumeRepository.findByIdForUpdate(insume.id) } returns insume
        every { insumeRepository.save(any()) } returns insume

        insumeService.deductStock(insume.id, 3)

        assertThat(insume.quantity).isEqualTo(7)
        verify(exactly = 1) { insumeRepository.save(insume) }
    }

    @Test
    fun `deductStock insufficient stock - throws InsufficientStockException`() {
        val insume = buildInsume(quantity = 0)

        every { insumeRepository.findByIdForUpdate(insume.id) } returns insume

        assertThatThrownBy { insumeService.deductStock(insume.id, 1) }
            .isInstanceOf(InsufficientStockException::class.java)

        verify(exactly = 0) { insumeRepository.save(any()) }
    }

    @Test
    fun `deductStock not found - throws EntityNotFoundException`() {
        val id = UUID.randomUUID()

        every { insumeRepository.findByIdForUpdate(id) } returns null

        assertThatThrownBy { insumeService.deductStock(id, 1) }
            .isInstanceOf(EntityNotFoundException::class.java)
    }

    // -------------------------------------------------------------------------
    // restoreStock
    // -------------------------------------------------------------------------

    @Test
    fun `restoreStock success - quantity restored`() {
        val insume = buildInsume(quantity = 5)

        every { insumeRepository.findByIdForUpdate(insume.id) } returns insume
        every { insumeRepository.save(any()) } returns insume

        insumeService.restoreStock(insume.id, 3)

        assertThat(insume.quantity).isEqualTo(8)
        verify(exactly = 1) { insumeRepository.save(insume) }
    }

    @Test
    fun `restoreStock not found - throws EntityNotFoundException`() {
        val id = UUID.randomUUID()

        every { insumeRepository.findByIdForUpdate(id) } returns null

        assertThatThrownBy { insumeService.restoreStock(id, 1) }
            .isInstanceOf(EntityNotFoundException::class.java)
    }
}
