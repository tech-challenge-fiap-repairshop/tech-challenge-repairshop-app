package com.cao.repairshop.execution.entity

import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.core.exception.InvalidStateTransitionException
import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.inventory.entity.Insume
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Entity
@Table(name = "tb_execution")
class Execution(
    @Id
    @Column(name = "id_tb_execution")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_order", nullable = false)
    var serviceOrder: ServiceOrder,

    @Enumerated(EnumType.STRING)
    @Column(name = "basic_description", nullable = false, length = 50)
    var basicDescription: BasicExecution,

    @Column(name = "full_description", columnDefinition = "TEXT")
    var fullDescription: String? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,

    @Column(name = "estimated_time", precision = 5, scale = 2)
    var estimatedTime: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ExecutionStatus = ExecutionStatus.INITIATED,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "execution_id", insertable = false, updatable = false)
    var histories: MutableSet<ExecutionHistory> = mutableSetOf(),

    @OneToMany(mappedBy = "execution", cascade = [CascadeType.ALL], orphanRemoval = true)
    var insumes: MutableSet<ExecutionInsume> = mutableSetOf(),

    @CreationTimestamp
    @Column(nullable = false)
    var created: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updated: LocalDateTime? = null
) {

    fun advanceStatus(newStatus: ExecutionStatus) {
        if (newStatus !in status.allowedTransitions())
            throw InvalidStateTransitionException(ErrorMessages.StateTransition.invalid(status, newStatus))
        status = newStatus
        recordHistory(newStatus)
    }

    fun addInsume(insume: Insume, quantity: Int) {
        val alreadyLinked = insumes.any { it.insume.id == insume.id }
        if (alreadyLinked)
            throw IllegalArgumentException("Insume ${insume.id} is already linked to this execution.")

        insumes.add(
            ExecutionInsume(
                id = ExecutionInsumeId(id, insume.id),
                execution = this,
                insume = insume,
                quantity = quantity
            )
        )
    }

    fun recordHistory(newStatus: ExecutionStatus) {
        val lastHistory = histories.maxByOrNull { it.registerTime }
        val now = LocalDateTime.now()
        val interval = lastHistory?.let { ChronoUnit.SECONDS.between(it.registerTime, now) }

        histories.add(
            ExecutionHistory(
                execution = this,
                status = newStatus,
                registerTime = now,
                intervalTime = interval
            )
        )
    }

    override fun equals(other: Any?): Boolean = other is Execution && id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "Execution(id=$id, basicDescription=$basicDescription)"
}

