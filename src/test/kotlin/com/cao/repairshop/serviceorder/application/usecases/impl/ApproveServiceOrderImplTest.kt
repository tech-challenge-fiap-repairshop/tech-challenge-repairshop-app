package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.inventory.application.usecases.DeductInsumeStock
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.domain.ApprovalDomainService
import com.cao.repairshop.serviceorder.domain.ApprovalDomainService.StockRequirement
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import com.cao.repairshop.serviceorder.infra.controller.dtos.ApprovalRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class ApproveServiceOrderImplTest {

    private lateinit var serviceOrderGateway: ServiceOrderGateway
    private lateinit var deductInsumeStock: DeductInsumeStock
    private lateinit var approvalDomainService: ApprovalDomainService
    private lateinit var approveServiceOrderImpl: ApproveServiceOrderImpl

    @BeforeEach
    fun setup() {
        serviceOrderGateway = mockk()
        deductInsumeStock = mockk(relaxed = true)
        approvalDomainService = mockk()
        approveServiceOrderImpl = ApproveServiceOrderImpl(serviceOrderGateway, deductInsumeStock, approvalDomainService)
    }

    @Test
    fun `should approve service order and deduct stock`() {
        val orderId = UUID.randomUUID()
        val request = ApprovalRequest(approved = true)

        val serviceOrder = ServiceOrder(
            id = orderId,
            customerId = UUID.randomUUID(),
            vehicleId = UUID.randomUUID(),
            status = ServiceOrderStatus.WAITING_APPROVAL,
            enterTime = LocalDateTime.now()
        )

        val insumeId = UUID.randomUUID()
        val stockRequirements = listOf(StockRequirement(insumeId, 2))

        every { serviceOrderGateway.findDetailedById(orderId) } returns serviceOrder
        every { approvalDomainService.approve(serviceOrder) } returns stockRequirements
        every { serviceOrderGateway.save(any()) } answers { firstArg() }

        val response = approveServiceOrderImpl.execute(orderId, request)

        assertEquals(ServiceOrderStatus.WAITING_APPROVAL, response.status) // Status logic handled by domain service
        verify { deductInsumeStock.execute(insumeId, 2) }
        verify { serviceOrderGateway.save(serviceOrder) }
    }

    @Test
    fun `should refuse service order`() {
        val orderId = UUID.randomUUID()
        val request = ApprovalRequest(approved = false)

        val serviceOrder = ServiceOrder(
            id = orderId,
            customerId = UUID.randomUUID(),
            vehicleId = UUID.randomUUID(),
            status = ServiceOrderStatus.WAITING_APPROVAL,
            enterTime = LocalDateTime.now()
        )

        every { serviceOrderGateway.findDetailedById(orderId) } returns serviceOrder
        every { approvalDomainService.refuse(serviceOrder) } returns Unit
        every { serviceOrderGateway.save(any()) } answers { firstArg() }

        approveServiceOrderImpl.execute(orderId, request)

        verify { approvalDomainService.refuse(serviceOrder) }
        verify(exactly = 0) { deductInsumeStock.execute(any(), any()) }
        verify { serviceOrderGateway.save(serviceOrder) }
    }

    @Test
    fun `should throw error when order not found`() {
        every { serviceOrderGateway.findDetailedById(any()) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            approveServiceOrderImpl.execute(UUID.randomUUID(), ApprovalRequest(approved = true))
        }
    }
}
