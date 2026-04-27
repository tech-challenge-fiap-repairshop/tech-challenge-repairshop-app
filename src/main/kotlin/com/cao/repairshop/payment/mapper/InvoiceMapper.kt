package com.cao.repairshop.payment.mapper

import com.cao.repairshop.payment.dto.InvoiceResponse
import com.cao.repairshop.payment.entity.Invoice

fun Invoice.toResponse() = InvoiceResponse(
    id = id,
    customerId = customer.id,
    customerName = customer.name,
    serviceOrderId = serviceOrder.id,
    invoiceNumber = invoiceNumber,
    price = price,
    emissionDate = emissionDate,
    created = created,
    updated = updated
)
