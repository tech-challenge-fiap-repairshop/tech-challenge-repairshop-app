package com.cao.repairshop.payment.application.usecases

import com.cao.repairshop.payment.infra.controller.dtos.InvoiceResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface FindInvoice {
    fun findById(id: UUID): InvoiceResponse
    fun findAll(pageable: Pageable): Page<InvoiceResponse>
}

