package com.cao.repairshop.payment.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.payment.application.gateways.InvoiceGateway
import com.cao.repairshop.payment.application.usecases.FindInvoice
import com.cao.repairshop.payment.domain.entities.mapper.toResponse
import com.cao.repairshop.payment.infra.controller.dtos.InvoiceResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FindInvoiceImpl(
    private val invoiceGateway: InvoiceGateway
) : FindInvoice {
    @Transactional(readOnly = true)
    override fun findById(id: UUID): InvoiceResponse {
        val invoice = invoiceGateway.findById(id) ?: throw EntityNotFoundException(ErrorMessages.Invoice.NOT_FOUND)
        return invoice.toResponse()
    }

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<InvoiceResponse> {
        return invoiceGateway.findAll(pageable).map { it.toResponse() }
    }
}

