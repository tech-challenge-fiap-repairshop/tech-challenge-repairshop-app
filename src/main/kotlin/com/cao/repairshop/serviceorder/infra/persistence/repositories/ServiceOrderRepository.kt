package com.cao.repairshop.serviceorder.infra.persistence.repositories

import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface ServiceOrderRepository : JpaRepository<ServiceOrderEntity, UUID> {
    @EntityGraph(attributePaths = ["customer", "vehicle", "executions", "executions.insumes", "executions.insumes.insume", "histories"])
    @Query(
        value = """
            SELECT so FROM ServiceOrderEntity so 
            WHERE so.status NOT IN ('FINALIZED', 'PAID', 'CANCELED')
            ORDER BY 
                CASE so.status 
                    WHEN 'IN_EXECUTION' THEN 1 
                    WHEN 'WAITING_APPROVAL' THEN 2 
                    WHEN 'IN_DIAGNOSIS' THEN 3 
                    WHEN 'RECEIVED' THEN 4 
                    ELSE 5 
                END ASC,
                so.enterTime ASC
        """,
        countQuery = """
            SELECT COUNT(so) FROM ServiceOrderEntity so 
            WHERE so.status NOT IN ('FINALIZED', 'PAID', 'CANCELED')
        """
    )
    override fun findAll(pageable: Pageable): Page<ServiceOrderEntity>


    @EntityGraph(attributePaths = ["customer", "vehicle", "executions", "executions.insumes", "executions.insumes.insume", "histories"])
    fun findDetailedById(id: UUID): Optional<ServiceOrderEntity>

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
