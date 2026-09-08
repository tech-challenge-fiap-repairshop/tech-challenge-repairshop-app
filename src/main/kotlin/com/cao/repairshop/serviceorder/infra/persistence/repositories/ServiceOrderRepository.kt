package com.cao.repairshop.serviceorder.infra.persistence.repositories

import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface ServiceOrderRepository : JpaRepository<ServiceOrderEntity, UUID>, JpaSpecificationExecutor<ServiceOrderEntity> {
    @EntityGraph(attributePaths = ["customer", "vehicle"])
    override fun findAll(spec: Specification<ServiceOrderEntity>, pageable: Pageable): Page<ServiceOrderEntity>

    @EntityGraph(attributePaths = ["customer", "vehicle", "executions", "executions.insumes", "executions.insumes.insume", "histories"])
    fun findDetailedById(id: UUID): Optional<ServiceOrderEntity>

    fun existsByCustomerId(customerId: UUID): Boolean
    fun existsByVehicleId(vehicleId: UUID): Boolean
    fun countByStatus(status: ServiceOrderStatus): Long

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

    @Query(
        nativeQuery = true,
        value = """
            SELECT AVG(EXTRACT(EPOCH FROM (h2.register_time - h1.register_time))) / 60.0
            FROM tb_service_order_history h1
            INNER JOIN tb_service_order_history h2
                ON h1.service_order_id = h2.service_order_id
            WHERE h1.status = :fromStatus
              AND h2.status = :toStatus
              AND h2.register_time >= h1.register_time
        """
    )
    fun getAverageStageDurationMinutes(@Param("fromStatus") fromStatus: String, @Param("toStatus") toStatus: String): Double?

    @Query(
        nativeQuery = true,
        value = """
            SELECT AVG(EXTRACT(EPOCH FROM (h_paid.register_time - h_rec.register_time))) / 60.0
            FROM tb_service_order_history h_rec
            INNER JOIN tb_service_order_history h_paid
                ON h_rec.service_order_id = h_paid.service_order_id
            WHERE h_rec.status = 'RECEIVED'
              AND h_paid.status = 'PAID'
              AND h_paid.register_time >= h_rec.register_time
        """
    )
    fun getAverageLeadTimeMinutes(): Double?
}
