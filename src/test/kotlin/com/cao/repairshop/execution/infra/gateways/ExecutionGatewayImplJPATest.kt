package com.cao.repairshop.execution.infra.gateways

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.execution.domain.entities.Execution
import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.execution.infra.persistence.models.ExecutionEntity
import com.cao.repairshop.execution.infra.persistence.repositories.ExecutionRepository
import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import com.cao.repairshop.serviceorder.infra.persistence.repositories.ServiceOrderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

class ExecutionGatewayImplJPATest {

    private lateinit var executionRepository: ExecutionRepository
    private lateinit var serviceOrderRepository: ServiceOrderRepository
    private lateinit var executionGatewayImplJPA: ExecutionGatewayImplJPA

    @BeforeEach
    fun setup() {
        executionRepository = mockk()
        serviceOrderRepository = mockk()
        executionGatewayImplJPA = ExecutionGatewayImplJPA(executionRepository, serviceOrderRepository)
    }

    @Test
    fun `should save execution successfully`() {
        val serviceOrderId = UUID.randomUUID()
        val execution = mockk<Execution>(relaxed = true)
        every { execution.serviceOrderId } returns serviceOrderId
        
        val serviceOrderEntity = mockk<ServiceOrderEntity>(relaxed = true)
        every { serviceOrderEntity.id } returns serviceOrderId
        val executionEntity = mockk<ExecutionEntity>(relaxed = true)
        
        every { serviceOrderRepository.findById(serviceOrderId) } returns Optional.of(serviceOrderEntity)
        every { executionRepository.save(any()) } returns executionEntity
        every { executionEntity.id } returns UUID.randomUUID()
        every { executionEntity.serviceOrder.id } returns serviceOrderId
        every { executionEntity.basicDescription } returns BasicExecution.OIL_CHANGE
        every { executionEntity.price } returns BigDecimal.TEN
        every { executionEntity.status } returns ExecutionStatus.INITIATED

        val result = executionGatewayImplJPA.save(execution)
        assertNotNull(result)
        verify { executionRepository.save(any()) }
    }

    @Test
    fun `should throw EntityNotFoundException when saving execution with invalid service order`() {
        val serviceOrderId = UUID.randomUUID()
        val execution = mockk<Execution>(relaxed = true)
        every { execution.serviceOrderId } returns serviceOrderId

        every { serviceOrderRepository.findById(serviceOrderId) } returns Optional.empty()

        assertThrows(EntityNotFoundException::class.java) {
            executionGatewayImplJPA.save(execution)
        }
    }

    @Test
    fun `should find by id successfully`() {
        val id = UUID.randomUUID()
        val executionEntity = mockk<ExecutionEntity>(relaxed = true)
        every { executionEntity.id } returns id
        every { executionEntity.serviceOrder.id } returns UUID.randomUUID()
        every { executionEntity.basicDescription } returns BasicExecution.OIL_CHANGE
        every { executionEntity.price } returns BigDecimal.TEN
        every { executionEntity.status } returns ExecutionStatus.INITIATED
        every { executionRepository.findById(id) } returns Optional.of(executionEntity)

        val result = executionGatewayImplJPA.findById(id)
        assertNotNull(result)
        assertEquals(id, result?.id)
    }

    @Test
    fun `should find by service order id successfully`() {
        val serviceOrderId = UUID.randomUUID()
        val executionEntity = mockk<ExecutionEntity>(relaxed = true)
        every { executionEntity.id } returns UUID.randomUUID()
        every { executionEntity.serviceOrder.id } returns serviceOrderId
        every { executionEntity.basicDescription } returns BasicExecution.OIL_CHANGE
        every { executionEntity.price } returns BigDecimal.TEN
        every { executionEntity.status } returns ExecutionStatus.INITIATED
        every { executionRepository.findByServiceOrderId(serviceOrderId) } returns listOf(executionEntity)

        val result = executionGatewayImplJPA.findByServiceOrderId(serviceOrderId)
        assertEquals(1, result.size)
    }

    @Test
    fun `should delete execution successfully`() {
        val id = UUID.randomUUID()
        every { executionRepository.deleteById(id) } returns Unit

        executionGatewayImplJPA.delete(id)
        verify { executionRepository.deleteById(id) }
    }

    @Test
    fun `should get average execution time minutes`() {
        every { executionRepository.getAverageExecutionTimeMinutes() } returns 60.0
        val result = executionGatewayImplJPA.getAverageExecutionTimeMinutes()
        assertEquals(60.0, result)
    }
}
