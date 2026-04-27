package com.cao.repairshop.execution.domain

import com.cao.repairshop.core.exception.InvalidStateTransitionException
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

class ExecutionDomainTest {

    private fun buildExecution(status: ExecutionStatus = ExecutionStatus.INITIATED): Execution {
        val customer = Customer(name = "Test", document = Document("52998224725"))
        val vehicle = Vehicle(customer = customer, plate = Plate("ABC1234"), brand = "T", model = "C")
        val order = ServiceOrder(customer = customer, vehicle = vehicle)
        return Execution(serviceOrder = order, basicDescription = BasicExecution.OIL_CHANGE, price = BigDecimal("100"), status = status)
    }

    private fun buildInsume() = Insume(name = "Oil Filter", price = BigDecimal("25"), unityPrice = BigDecimal("25"), quantity = 10)

    @Test
    fun `advanceStatus valid transition INITIATED to PENDING records history`() {
        val exec = buildExecution(ExecutionStatus.INITIATED)
        exec.advanceStatus(ExecutionStatus.PENDING)
        assertThat(exec.status).isEqualTo(ExecutionStatus.PENDING)
        assertThat(exec.histories).hasSize(1)
    }

    @Test
    fun `advanceStatus invalid transition throws exception`() {
        val exec = buildExecution(ExecutionStatus.INITIATED)
        assertThatThrownBy { exec.advanceStatus(ExecutionStatus.FINALIZED) }
            .isInstanceOf(InvalidStateTransitionException::class.java)
    }

    @Test
    fun `addInsume links insume to execution`() {
        val exec = buildExecution()
        val insume = buildInsume()
        exec.addInsume(insume, 3)

        assertThat(exec.insumes).hasSize(1)
        assertThat(exec.insumes.first().quantity).isEqualTo(3)
    }

    @Test
    fun `addInsume duplicate insume throws exception`() {
        val exec = buildExecution()
        val insume = buildInsume()
        exec.addInsume(insume, 2)

        assertThatThrownBy { exec.addInsume(insume, 3) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("already linked")
    }

    @Test
    fun `recordHistory adds entry with status`() {
        val exec = buildExecution()
        exec.recordHistory(ExecutionStatus.INITIATED)
        assertThat(exec.histories).hasSize(1)
        assertThat(exec.histories.first().status).isEqualTo(ExecutionStatus.INITIATED)
    }

    @Test
    fun `recordHistory second call calculates interval`() {
        val exec = buildExecution()
        exec.recordHistory(ExecutionStatus.INITIATED)
        exec.recordHistory(ExecutionStatus.PENDING)

        val sorted = exec.histories.sortedBy { it.registerTime }
        assertThat(sorted.last().intervalTime).isNotNull
    }
}
