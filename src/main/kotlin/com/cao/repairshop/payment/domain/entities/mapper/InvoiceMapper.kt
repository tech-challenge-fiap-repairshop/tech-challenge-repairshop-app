package com.cao.repairshop.payment.domain.entities.mapper

import com.cao.repairshop.payment.domain.entities.Invoice
import com.cao.repairshop.payment.infra.persistence.models.InvoiceEntity
import com.cao.repairshop.payment.infra.controller.dtos.InvoiceResponse
import com.cao.repairshop.payment.infra.controller.dtos.InvoiceItemResponse
import com.cao.repairshop.payment.infra.controller.dtos.InvoiceInsumeResponse
import com.cao.repairshop.register.domain.entities.mapper.toDomain
import com.cao.repairshop.serviceorder.domain.entities.mapper.toDomain
import java.math.BigDecimal

fun InvoiceEntity.toDomain() = Invoice(
    id = id,
    customer = customer.toDomain(),
    serviceOrder = serviceOrder.toDomain(),
    price = price,
    invoiceNumber = invoiceNumber,
    emissionDate = emissionDate,
    vehiclePlate = serviceOrder.vehicle.plate.value,
    created = created,
    updated = updated
)

fun Invoice.toResponse() = InvoiceResponse(
    id = id,
    customerId = customer.id,
    customerName = customer.name,
    serviceOrderId = serviceOrder.id,
    vehiclePlate = vehiclePlate ?: "",
    serviceOrderStatus = serviceOrder.status.name,
    price = price,
    invoiceNumber = invoiceNumber,
    emissionDate = emissionDate,
    items = serviceOrder.executions.map { exec ->
        InvoiceItemResponse(
            description = exec.basicDescription.name,
            laborPrice = exec.price,
            insumes = exec.insumes.map { ei ->
                InvoiceInsumeResponse(
                    name = ei.insume.name,
                    quantity = ei.quantity,
                    unitPrice = ei.insume.unityPrice,
                    totalPrice = ei.insume.unityPrice.multiply(ei.quantity.toBigDecimal())
                )
            },
            totalItemPrice = exec.getTotalPrice()
        )
    },
    created = created,
    updated = updated
)
