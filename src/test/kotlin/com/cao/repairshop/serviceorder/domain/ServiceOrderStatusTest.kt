package com.cao.repairshop.serviceorder.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServiceOrderStatusTest {

    @Test
    fun `RECEIVED allows only IN_DIAGNOSIS`() {
        assertEquals(setOf(ServiceOrderStatus.IN_DIAGNOSIS), ServiceOrderStatus.RECEIVED.allowedTransitions())
    }

    @Test
    fun `IN_DIAGNOSIS allows only WAITING_APPROVAL`() {
        assertEquals(setOf(ServiceOrderStatus.WAITING_APPROVAL), ServiceOrderStatus.IN_DIAGNOSIS.allowedTransitions())
    }

    @Test
    fun `WAITING_APPROVAL allows APPROVED and REFUSED`() {
        assertEquals(setOf(ServiceOrderStatus.APPROVED, ServiceOrderStatus.REFUSED), ServiceOrderStatus.WAITING_APPROVAL.allowedTransitions())
    }

    @Test
    fun `APPROVED allows only IN_EXECUTION`() {
        assertEquals(setOf(ServiceOrderStatus.IN_EXECUTION), ServiceOrderStatus.APPROVED.allowedTransitions())
    }

    @Test
    fun `REFUSED allows only CANCELED`() {
        assertEquals(setOf(ServiceOrderStatus.CANCELED), ServiceOrderStatus.REFUSED.allowedTransitions())
    }

    @Test
    fun `IN_EXECUTION allows only FINALIZED`() {
        assertEquals(setOf(ServiceOrderStatus.FINALIZED), ServiceOrderStatus.IN_EXECUTION.allowedTransitions())
    }

    @Test
    fun `FINALIZED allows only PAID`() {
        assertEquals(setOf(ServiceOrderStatus.PAID), ServiceOrderStatus.FINALIZED.allowedTransitions())
    }

    @Test
    fun `PAID is terminal and allows nothing`() {
        assertEquals(emptySet<ServiceOrderStatus>(), ServiceOrderStatus.PAID.allowedTransitions())
    }

    @Test
    fun `CANCELED is terminal and allows nothing`() {
        assertEquals(emptySet<ServiceOrderStatus>(), ServiceOrderStatus.CANCELED.allowedTransitions())
    }
}
