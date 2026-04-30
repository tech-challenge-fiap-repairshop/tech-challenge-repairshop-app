package com.cao.repairshop.serviceorder.entity

import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.core.exception.InvalidStateTransitionException
import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.execution.entity.Execution
import com.cao.repairshop.execution.entity.ExecutionHistory
import com.cao.repairshop.inventory.entity.Insume
import com.cao.repairshop.payment.entity.Invoice
import com.cao.repairshop.register.entity.Customer
import com.cao.repairshop.register.entity.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Entity
@Table(name = "tb_service_order")
class ServiceOrder(
    @Id
    @Column(name = "id_tb_service_order")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    var customer: Customer,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: Vehicle,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: ServiceOrderStatus = ServiceOrderStatus.RECEIVED,

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    var totalPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "enter_time")
    var enterTime: LocalDateTime? = null,

    @Column(name = "end_time")
    var endTime: LocalDateTime? = null,

    @Column(name = "valid_date")
    var validDate: LocalDate? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "service_order_id", insertable = false, updatable = false)
    var histories: MutableSet<ServiceOrderHistory> = mutableSetOf(),

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "service_order", insertable = false, updatable = false)
    var executions: MutableSet<Execution> = mutableSetOf(),

    @OneToOne(mappedBy = "serviceOrder", cascade = [CascadeType.ALL], orphanRemoval = true)
    var invoice: Invoice? = null,

    @CreationTimestamp
    @Column(name = "created", updatable = false)
    var created: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated")
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
            serviceOrder = this,
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
                serviceOrder = this,
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

