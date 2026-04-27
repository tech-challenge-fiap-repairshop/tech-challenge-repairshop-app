package com.cao.repairshop.execution.repository

import com.cao.repairshop.execution.entity.Execution
import com.cao.repairshop.execution.entity.ExecutionHistory

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ExecutionRepository : JpaRepository<Execution, UUID> {
    fun findByServiceOrderId(serviceOrderId: UUID): List<Execution>
}
