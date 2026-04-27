package com.cao.repairshop.execution.entity

import com.cao.repairshop.inventory.entity.Insume
import jakarta.persistence.*

@Entity
@Table(name = "tb_execution_insume")
class ExecutionInsume(
    @EmbeddedId
    var id: ExecutionInsumeId,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("executionId")
    @JoinColumn(name = "id_tb_execution")
    var execution: Execution,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("insumeId")
    @JoinColumn(name = "id_tb_insume")
    var insume: Insume,

    @Column(name = "quantity_used", nullable = false)
    var quantity: Int = 1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExecutionInsume) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
