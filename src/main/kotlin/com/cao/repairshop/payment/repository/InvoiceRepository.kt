package com.cao.repairshop.payment.repository

import com.cao.repairshop.payment.entity.Invoice
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InvoiceRepository : JpaRepository<Invoice, UUID> {
    fun findByServiceOrderId(serviceOrderId: UUID): Invoice?
    fun findByInvoiceNumber(invoiceNumber: String): Invoice?
    fun existsByServiceOrderId(serviceOrderId: UUID): Boolean
}
