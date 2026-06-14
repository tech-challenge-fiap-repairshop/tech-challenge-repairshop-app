package com.cao.repairshop.payment.application.usecases

import com.cao.repairshop.payment.infra.controller.dtos.CreateInvoiceRequest
import com.cao.repairshop.payment.infra.controller.dtos.InvoiceResponse

fun interface CreateInvoice {
    fun execute(request: CreateInvoiceRequest): InvoiceResponse
}

