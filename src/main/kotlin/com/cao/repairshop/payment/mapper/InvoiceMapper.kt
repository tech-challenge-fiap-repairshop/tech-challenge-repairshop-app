package com.cao.repairshop.payment.mapper

import com.cao.repairshop.payment.dto.InvoiceInsumeResponse
import com.cao.repairshop.payment.dto.InvoiceItemResponse
import com.cao.repairshop.payment.dto.InvoiceResponse
import com.cao.repairshop.payment.entity.Invoice
import java.math.BigDecimal

fun Invoice.toResponse() = InvoiceResponse(
    id = id,
    customerId = customer.id,
    customerName = customer.name,
    serviceOrderId = serviceOrder.id,
    vehiclePlate = serviceOrder.vehicle.plate.value,
    serviceOrderStatus = serviceOrder.status.name,
    invoiceNumber = invoiceNumber,
    price = price,
    emissionDate = emissionDate,
    items = serviceOrder.executions.map { exec ->
        InvoiceItemResponse(
            description = exec.basicDescription.name,
            laborPrice = exec.price,
            insumes = exec.insumes.map { ei ->
                InvoiceInsumeResponse(
                    name = ei.insume.name,
                    quantity = ei.quantity,
                    unitPrice = ei.insume.price,
                    totalPrice = ei.insume.price.multiply(BigDecimal.valueOf(ei.quantity.toLong()))
                )
            },
            totalItemPrice = exec.getTotalPrice()
        )
    },
    created = created,
    updated = updated
)
