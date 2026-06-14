package com.cao.repairshop.payment.application.usecases

import com.cao.repairshop.payment.infra.controller.dtos.InvoiceResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface FindAllInvoices {
    fun execute(pageable: Pageable): Page<InvoiceResponse>
}

