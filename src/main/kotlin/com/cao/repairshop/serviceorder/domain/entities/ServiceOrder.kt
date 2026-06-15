package com.cao.repairshop.serviceorder.domain.entities

import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.core.exception.InvalidStateTransitionException
import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.execution.domain.entities.Execution
import com.cao.repairshop.inventory.domain.entities.Insume
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class ServiceOrder(
    val id: UUID = UUID.randomUUID(),
    var customerId: UUID,
    var vehicleId: UUID,
    var status: ServiceOrderStatus = ServiceOrderStatus.RECEIVED,
    var totalPrice: BigDecimal = BigDecimal.ZERO,
    var enterTime: LocalDateTime? = null,
    var endTime: LocalDateTime? = null,
    var validDate: LocalDate? = null,
    var histories: MutableSet<ServiceOrderHistory> = mutableSetOf(),
    var executions: MutableSet<Execution> = mutableSetOf(),
    var invoiceId: UUID? = null,
    var created: LocalDateTime? = null,
    var updated: LocalDateTime? = null
) {

    fun advanceStatus(newStatus: ServiceOrderStatus, description: String? = null) {
        if (newStatus !in status.allowedTransitions())
            throw InvalidStateTransitionException(ErrorMessages.StateTransition.invalid(status, newStatus))

        if (newStatus == ServiceOrderStatus.WAITING_APPROVAL && executions.isEmpty())
            throw InvalidStateTransitionException("Cannot send to approval: no services were registered during diagnosis.")

        if (newStatus == ServiceOrderStatus.FINALIZED) {
            val allFinalized = executions.all { it.status == ExecutionStatus.FINALIZED }
            if (!allFinalized)
                throw InvalidStateTransitionException(ErrorMessages.ServiceOrder.NOT_ALL_FINALIZED)
            endTime = LocalDateTime.now()
        }

        status = newStatus
        recordHistory(newStatus, description ?: newStatus.defaultMessage)
    }

    fun approve() {
        advanceStatus(ServiceOrderStatus.APPROVED)
    }

    fun refuse() {
        advanceStatus(ServiceOrderStatus.REFUSED)
    }

    fun addExecution(
        basicDescription: BasicExecution,
        fullDescription: String?,
        price: BigDecimal,
        estimatedTime: BigDecimal?,
        insumes: List<Pair<Insume, Int>>
    ): Execution {
        val execution = Execution(
            serviceOrderId = this.id,
            basicDescription = basicDescription,
            fullDescription = fullDescription,
            price = price,
            estimatedTime = estimatedTime
        )
        execution.recordHistory(execution.status)
        insumes.forEach { (insume, qty) -> execution.addInsume(insume, qty) }
        executions.add(execution)
        recalculateTotalPrice()
        return execution
    }

    fun collectInsumeRequirements(): List<Pair<UUID, Int>> =
        executions.flatMap { exec ->
            exec.insumes.map { it.insume.id to it.quantity }
        }

    fun recalculateTotalPrice() {
        totalPrice = executions.fold(BigDecimal.ZERO) { acc, exec -> acc.add(exec.getTotalPrice()) }
    }

    fun checkCompletion() {
        takeIf { status == ServiceOrderStatus.IN_EXECUTION && executions.all { it.status == ExecutionStatus.FINALIZED } }
            ?.advanceStatus(ServiceOrderStatus.FINALIZED)
    }

    fun recordHistory(newStatus: ServiceOrderStatus, description: String? = null) {
        val lastHistory = histories.maxByOrNull { it.registerTime }
        val now = LocalDateTime.now()
        val interval = lastHistory?.let { ChronoUnit.SECONDS.between(it.registerTime, now) }

        histories.add(
            ServiceOrderHistory(
                id = UUID.randomUUID(),
                serviceOrderId = id,
                status = newStatus,
                description = description,
                registerTime = now,
                intervalTime = interval
            )
        )
    }

    override fun equals(other: Any?): Boolean = other is ServiceOrder && id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "ServiceOrder(id=$id, status=$status)"
}
