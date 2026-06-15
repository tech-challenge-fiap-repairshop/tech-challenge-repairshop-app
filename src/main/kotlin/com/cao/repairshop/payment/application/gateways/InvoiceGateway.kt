package com.cao.repairshop.payment.application.gateways

import com.cao.repairshop.payment.domain.entities.Invoice
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface InvoiceGateway {
    fun save(invoice: Invoice): Invoice
    fun findById(id: UUID): Invoice?
    fun findAll(pageable: Pageable): Page<Invoice>
    fun findByServiceOrderId(serviceOrderId: UUID): Invoice?
    fun findByInvoiceNumber(invoiceNumber: String): Invoice?
}
