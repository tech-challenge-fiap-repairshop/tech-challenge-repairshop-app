package com.cao.repairshop.execution.infra.persistence.models

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
class ExecutionInsumeId(
    @Column(name = "id_tb_execution")
    var executionId: UUID,

    @Column(name = "id_tb_insume")
    var insumeId: UUID
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExecutionInsumeId) return false
        return executionId == other.executionId && insumeId == other.insumeId
    }

    override fun hashCode(): Int {
        var result = executionId.hashCode()
        result = 31 * result + insumeId.hashCode()
        return result
    }
}
