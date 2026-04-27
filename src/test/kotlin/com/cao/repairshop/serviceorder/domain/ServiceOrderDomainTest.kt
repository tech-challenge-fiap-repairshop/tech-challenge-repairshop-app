package com.cao.repairshop.serviceorder.domain

import com.cao.repairshop.core.exception.InvalidStateTransitionException
import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.execution.entity.Execution
import com.cao.repairshop.inventory.entity.Insume
import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Plate
import com.cao.repairshop.register.entity.Customer
import com.cao.repairshop.register.entity.Vehicle
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class ServiceOrderDomainTest {

    private fun buildOrder(status: ServiceOrderStatus = ServiceOrderStatus.RECEIVED): ServiceOrder {
        val customer = Customer(name = "Test", document = Document("52998224725"))
        val vehicle = Vehicle(customer = customer, plate = Plate("ABC1234"), brand = "T", model = "C")
        return ServiceOrder(customer = customer, vehicle = vehicle, status = status, enterTime = LocalDateTime.now())
    }

    private fun buildInsume() = Insume(name = "Oil Filter", price = BigDecimal("25.00"), unityPrice = BigDecimal("25.00"), quantity = 10)

    @Test
    fun `advanceStatus valid transition records history`() {
        val order = buildOrder(ServiceOrderStatus.RECEIVED)
        order.advanceStatus(ServiceOrderStatus.IN_DIAGNOSIS)
        assertThat(order.status).isEqualTo(ServiceOrderStatus.IN_DIAGNOSIS)
        assertThat(order.histories).hasSize(1)
    }

    @Test
    fun `advanceStatus invalid transition throws exception`() {
        val order = buildOrder(ServiceOrderStatus.RECEIVED)
        assertThatThrownBy { order.advanceStatus(ServiceOrderStatus.FINALIZED) }
            .isInstanceOf(InvalidStateTransitionException::class.java)
    }

    @Test
    fun `advanceStatus to WAITING_APPROVAL without executions throws exception`() {
        val order = buildOrder(ServiceOrderStatus.IN_DIAGNOSIS)
        assertThatThrownBy { order.advanceStatus(ServiceOrderStatus.WAITING_APPROVAL) }
            .isInstanceOf(InvalidStateTransitionException::class.java)
            .hasMessageContaining("no services")
    }

    @Test
    fun `advanceStatus to FINALIZED requires all executions FINALIZED`() {
        val order = buildOrder(ServiceOrderStatus.IN_EXECUTION)
        order.addExecution(BasicExecution.OIL_CHANGE, null, BigDecimal("100"), null, emptyList())

        assertThatThrownBy { order.advanceStatus(ServiceOrderStatus.FINALIZED) }
            .isInstanceOf(InvalidStateTransitionException::class.java)
            .hasMessageContaining("FINALIZED")
    }

    @Test
    fun `advanceStatus to FINALIZED sets endTime`() {
        val order = buildOrder(ServiceOrderStatus.IN_EXECUTION)
        val exec = order.addExecution(BasicExecution.OIL_CHANGE, null, BigDecimal("100"), null, emptyList())
        exec.status = ExecutionStatus.FINALIZED

        order.advanceStatus(ServiceOrderStatus.FINALIZED)
        assertThat(order.endTime).isNotNull
        assertThat(order.status).isEqualTo(ServiceOrderStatus.FINALIZED)
    }

    @Test
    fun `approve transitions from WAITING_APPROVAL to APPROVED`() {
        val order = buildOrder(ServiceOrderStatus.WAITING_APPROVAL)
        order.addExecution(BasicExecution.OIL_CHANGE, null, BigDecimal("100"), null, emptyList())
        order.approve()
        assertThat(order.status).isEqualTo(ServiceOrderStatus.APPROVED)
    }

    @Test
    fun `refuse transitions from WAITING_APPROVAL to REFUSED`() {
        val order = buildOrder(ServiceOrderStatus.WAITING_APPROVAL)
        order.refuse()
        assertThat(order.status).isEqualTo(ServiceOrderStatus.REFUSED)
    }

    @Test
    fun `addExecution creates execution and recalculates price`() {
        val order = buildOrder()
        order.addExecution(BasicExecution.OIL_CHANGE, "Full oil change", BigDecimal("150.00"), null, emptyList())
        order.addExecution(BasicExecution.BRAKE_INSPECTION, null, BigDecimal("200.00"), null, emptyList())

        assertThat(order.executions).hasSize(2)
        assertThat(order.totalPrice).isEqualByComparingTo(BigDecimal("350.00"))
    }

    @Test
    fun `addExecution with insumes links them correctly`() {
        val order = buildOrder()
        val insume = buildInsume()
        order.addExecution(BasicExecution.OIL_CHANGE, null, BigDecimal("100"), null, listOf(insume to 2))

        val exec = order.executions.first()
        assertThat(exec.insumes).hasSize(1)
        assertThat(exec.insumes.first().quantity).isEqualTo(2)
    }

    @Test
    fun `collectInsumeRequirements returns all insume pairs`() {
        val order = buildOrder()
        val insume1 = buildInsume()
        val insume2 = Insume(name = "Brake Pad", price = BigDecimal("50"), unityPrice = BigDecimal("50"), quantity = 5)

        order.addExecution(BasicExecution.OIL_CHANGE, null, BigDecimal("100"), null, listOf(insume1 to 2))
        order.addExecution(BasicExecution.BRAKE_INSPECTION, null, BigDecimal("200"), null, listOf(insume2 to 1))

        val requirements = order.collectInsumeRequirements()
        assertThat(requirements).hasSize(2)
    }

    @Test
    fun `recordHistory calculates interval from previous entry`() {
        val order = buildOrder()
        order.recordHistory(ServiceOrderStatus.RECEIVED)
        order.recordHistory(ServiceOrderStatus.IN_DIAGNOSIS)

        assertThat(order.histories).hasSize(2)
        val sorted = order.histories.sortedBy { it.registerTime }
        assertThat(sorted.last().intervalTime).isNotNull
    }

    @Test
    fun `recalculateTotalPrice sums all execution prices`() {
        val order = buildOrder()
        order.addExecution(BasicExecution.OIL_CHANGE, null, BigDecimal("100"), null, emptyList())
        order.addExecution(BasicExecution.BRAKE_INSPECTION, null, BigDecimal("250"), null, emptyList())

        order.recalculateTotalPrice()
        assertThat(order.totalPrice).isEqualByComparingTo(BigDecimal("350"))
    }
}
