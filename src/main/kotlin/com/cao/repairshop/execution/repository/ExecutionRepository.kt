package com.cao.repairshop.execution.repository

import com.cao.repairshop.execution.entity.Execution
import com.cao.repairshop.execution.entity.ExecutionHistory

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ExecutionRepository : JpaRepository<Execution, UUID> {
    fun findByServiceOrderId(serviceOrderId: UUID): List<Execution>

    @Query(
        nativeQuery = true,
        value = """
            SELECT AVG(EXTRACT(EPOCH FROM (finalized.register_time - pending.register_time))) / 60.0
            FROM tb_execution_history pending
            INNER JOIN tb_execution_history finalized
                ON pending.execution_id = finalized.execution_id
            WHERE pending.status = 'PENDING'
            AND finalized.status = 'FINALIZED'
            AND finalized.register_time > pending.register_time
        """
    )
    fun getAverageExecutionTimeMinutes(): Double?
}
