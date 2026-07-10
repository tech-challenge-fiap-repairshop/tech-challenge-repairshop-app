package com.cao.repairshop.payment.infra.persistence.repositories

import com.cao.repairshop.payment.infra.persistence.models.InvoiceEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface InvoiceRepository : JpaRepository<InvoiceEntity, UUID> {
    @EntityGraph(attributePaths = ["customer", "serviceOrder", "serviceOrder.executions", "serviceOrder.executions.insumes", "serviceOrder.executions.insumes.insume"])
    fun findDetailedById(id: UUID): Optional<InvoiceEntity>

    @EntityGraph(attributePaths = ["customer", "serviceOrder", "serviceOrder.vehicle"])
    override fun findAll(pageable: Pageable): Page<InvoiceEntity>

    fun findByServiceOrderId(serviceOrderId: UUID): InvoiceEntity?
    fun findByInvoiceNumber(invoiceNumber: String): InvoiceEntity?
    fun existsByServiceOrderId(serviceOrderId: UUID): Boolean
}


