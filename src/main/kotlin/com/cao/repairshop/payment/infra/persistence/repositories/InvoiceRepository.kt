package com.cao.repairshop.payment.infra.persistence.repositories

import com.cao.repairshop.payment.infra.persistence.models.InvoiceEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface InvoiceRepository : JpaRepository<InvoiceEntity, UUID> {
    @EntityGraph(attributePaths = ["customer", "serviceOrder", "serviceOrder.executions", "serviceOrder.executions.insumes", "serviceOrder.executions.insumes.insume"])
    fun findDetailedById(id: UUID): Optional<InvoiceEntity>

    @EntityGraph(attributePaths = ["customer", "serviceOrder.vehicle", "serviceOrder", "serviceOrder.executions", "serviceOrder.executions.insumes", "serviceOrder.executions.insumes.insume"])
    override fun findAll(pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<InvoiceEntity>

    fun findByServiceOrderId(serviceOrderId: UUID): InvoiceEntity?
    fun findByInvoiceNumber(invoiceNumber: String): InvoiceEntity?
    fun existsByServiceOrderId(serviceOrderId: UUID): Boolean
}


