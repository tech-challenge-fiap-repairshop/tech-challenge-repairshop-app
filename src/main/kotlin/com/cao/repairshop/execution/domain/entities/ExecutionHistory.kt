package com.cao.repairshop.execution.domain.entities

import com.cao.repairshop.execution.domain.ExecutionStatus
import java.time.LocalDateTime
import java.util.UUID

class ExecutionHistory(
    val id: UUID = UUID.randomUUID(),
    val executionId: UUID,
    var status: ExecutionStatus,
    var description: String? = null,
    var registerTime: LocalDateTime = LocalDateTime.now(),
    var intervalTime: Long? = null
) {
    override fun equals(other: Any?): Boolean = other is ExecutionHistory && id == other.id
    override fun hashCode(): Int = id.hashCode()
}
