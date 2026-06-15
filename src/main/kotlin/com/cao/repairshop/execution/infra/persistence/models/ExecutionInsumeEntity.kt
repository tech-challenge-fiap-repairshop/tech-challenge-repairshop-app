package com.cao.repairshop.execution.infra.persistence.models

import com.cao.repairshop.inventory.infra.persistence.models.InsumeEntity
import jakarta.persistence.*

@Entity
@Table(name = "tb_execution_insume")
class ExecutionInsumeEntity(
    @EmbeddedId
    var id: ExecutionInsumeId,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("executionId")
    @JoinColumn(name = "id_tb_execution")
    var execution: ExecutionEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("insumeId")
    @JoinColumn(name = "id_tb_insume")
    var insume: InsumeEntity,

    @Column(name = "quantity_used", nullable = false)
    var quantity: Int = 1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExecutionInsumeEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
