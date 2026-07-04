package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.inventory.domain.entities.Insume
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.application.usecases.NotifyCustomer
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class AdvanceServiceOrderStatusImplTest {

    private lateinit var serviceOrderGateway: ServiceOrderGateway
    private lateinit var notifyCustomer: NotifyCustomer
    private lateinit var advanceServiceOrderStatusImpl: AdvanceServiceOrderStatusImpl

    @BeforeEach
    fun setup() {
        serviceOrderGateway = mockk()
        notifyCustomer = mockk(relaxed = true)
        advanceServiceOrderStatusImpl = AdvanceServiceOrderStatusImpl(
            serviceOrderGateway,
            notifyCustomer
        )
    }

    @Test
    fun `should advance status and notify customer when WAITING_APPROVAL`() {
        val orderId = UUID.randomUUID()
        val customerId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        
        val order = ServiceOrder(
            id = orderId,
            customerId = customerId,
            vehicleId = vehicleId,
            status = ServiceOrderStatus.IN_DIAGNOSIS,
            enterTime = LocalDateTime.now()
        )
        // Add an execution so WAITING_APPROVAL validation passes
        val insume = Insume(name = "Test", price = BigDecimal.TEN, unityPrice = BigDecimal.TEN, quantity = 10)
        order.addExecution(BasicExecution.OIL_CHANGE, null, BigDecimal("100"), null, listOf(insume to 1))

        every { serviceOrderGateway.findDetailedById(orderId) } returns order
        every { serviceOrderGateway.save(any()) } answers { firstArg() }

        val response = advanceServiceOrderStatusImpl.execute(orderId, ServiceOrderStatus.WAITING_APPROVAL)

        assertEquals(ServiceOrderStatus.WAITING_APPROVAL, response.status)
        verify { serviceOrderGateway.save(order) }
        verify { notifyCustomer.execute(any(), ServiceOrderStatus.WAITING_APPROVAL) }
    }

    @Test
    fun `should throw error when order not found`() {
        val orderId = UUID.randomUUID()
        every { serviceOrderGateway.findDetailedById(orderId) } returns null

        assertThrows(EntityNotFoundException::class.java) {
            advanceServiceOrderStatusImpl.execute(orderId, ServiceOrderStatus.IN_DIAGNOSIS)
        }
    }
}
