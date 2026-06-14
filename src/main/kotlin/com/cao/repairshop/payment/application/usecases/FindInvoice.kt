package com.cao.repairshop.payment.application.usecases

import com.cao.repairshop.payment.infra.controller.dtos.InvoiceResponse
import java.util.UUID

interface FindInvoice {
    fun execute(id: UUID): InvoiceResponse
}

