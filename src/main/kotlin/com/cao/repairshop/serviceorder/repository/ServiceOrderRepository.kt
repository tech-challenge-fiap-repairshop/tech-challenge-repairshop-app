package com.cao.repairshop.serviceorder.repository

import com.cao.repairshop.serviceorder.entity.ServiceOrder

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ServiceOrderRepository : JpaRepository<ServiceOrder, UUID> {
    fun existsByCustomerId(customerId: UUID): Boolean
    fun existsByVehicleId(vehicleId: UUID): Boolean

    @Query(
        nativeQuery = true,
        value = """
            SELECT AVG(EXTRACT(EPOCH FROM (finalized.register_time - in_execution.register_time))) / 60.0
            FROM tb_service_order_history in_execution
            INNER JOIN tb_service_order_history finalized
                ON in_execution.service_order_id = finalized.service_order_id
            WHERE in_execution.status = 'IN_EXECUTION'
            AND finalized.status = 'FINALIZED'
            AND finalized.register_time > in_execution.register_time
        """
    )
    fun getAverageExecutionTimeMinutes(): Double?
}
