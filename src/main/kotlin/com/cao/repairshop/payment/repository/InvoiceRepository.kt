package com.cao.repairshop.payment.repository

import com.cao.repairshop.payment.entity.Invoice
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface InvoiceRepository : JpaRepository<Invoice, UUID> {
    @EntityGraph(attributePaths = ["customer", "serviceOrder", "serviceOrder.executions", "serviceOrder.executions.insumes", "serviceOrder.executions.insumes.insume"])
    fun findDetailedById(id: UUID): Optional<Invoice>

    @EntityGraph(attributePaths = ["customer", "serviceOrder.vehicle", "serviceOrder", "serviceOrder.executions", "serviceOrder.executions.insumes", "serviceOrder.executions.insumes.insume"])
    override fun findAll(pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<Invoice>

    fun findByServiceOrderId(serviceOrderId: UUID): Invoice?
    fun findByInvoiceNumber(invoiceNumber: String): Invoice?
    fun existsByServiceOrderId(serviceOrderId: UUID): Boolean
}
