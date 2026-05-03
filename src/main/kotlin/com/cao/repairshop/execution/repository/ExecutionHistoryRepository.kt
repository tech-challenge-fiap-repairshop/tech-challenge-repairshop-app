package com.cao.repairshop.execution.repository

import com.cao.repairshop.execution.entity.ExecutionHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ExecutionHistoryRepository : JpaRepository<ExecutionHistory, UUID> {
    fun findByExecutionIdOrderByRegisterTimeAsc(executionId: UUID): List<ExecutionHistory>
}
