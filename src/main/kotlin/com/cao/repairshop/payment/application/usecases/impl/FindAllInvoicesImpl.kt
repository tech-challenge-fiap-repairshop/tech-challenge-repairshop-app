package com.cao.repairshop.payment.application.usecases.impl

import com.cao.repairshop.payment.application.gateways.InvoiceGateway
import com.cao.repairshop.payment.application.usecases.FindAllInvoices
import com.cao.repairshop.payment.domain.entities.mapper.toResponse
import com.cao.repairshop.payment.infra.controller.dtos.InvoiceResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FindAllInvoicesImpl(
    private val invoiceGateway: InvoiceGateway
) : FindAllInvoices {
    @Transactional(readOnly = true)
    override fun execute(pageable: Pageable): Page<InvoiceResponse> {
        return invoiceGateway.findAll(pageable).map { it.toResponse() }
    }
}

