package com.cao.repairshop.execution.infra.persistence.models

import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tb_execution")
class ExecutionEntity(
    @Id
    @Column(name = "id_tb_execution")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_order", nullable = false)
    var serviceOrder: ServiceOrderEntity,

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
    var histories: MutableSet<ExecutionHistoryEntity> = mutableSetOf(),

    @OneToMany(mappedBy = "execution", cascade = [CascadeType.ALL], orphanRemoval = true)
    var insumes: MutableSet<ExecutionInsumeEntity> = mutableSetOf(),

    @CreationTimestamp
    @Column(nullable = false)
    var created: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updated: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean = other is ExecutionEntity && id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "ExecutionEntity(id=$id, basicDescription=$basicDescription)"
}
