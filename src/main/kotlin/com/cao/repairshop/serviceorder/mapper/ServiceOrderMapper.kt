package com.cao.repairshop.serviceorder.mapper
 
import java.time.LocalDateTime
import java.util.UUID

import com.cao.repairshop.serviceorder.dto.ServiceOrderHistoryEntry
import com.cao.repairshop.serviceorder.dto.ServiceOrderResponse
import com.cao.repairshop.serviceorder.dto.ExecutionSummary
import com.cao.repairshop.serviceorder.dto.InsumeSummaryResponse
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import com.cao.repairshop.serviceorder.entity.ServiceOrderHistory
import java.math.BigDecimal

fun ServiceOrder.toResponse() = ServiceOrderResponse(
    id = id,
    customerId = customer.id,
    vehicleId = vehicle.id,
    status = status,
    totalPrice = totalPrice,
    enterTime = enterTime,
    endTime = endTime,
    created = created,
    updated = updated,
    services = executions.map { 
        ExecutionSummary(
            id = it.id,
            basicDescription = it.basicDescription,
            fullDescription = it.fullDescription,
            laborPrice = it.price,
            insumes = it.insumes.map { ei ->
                InsumeSummaryResponse(
                    name = ei.insume.name,
                    quantity = ei.quantity,
                    unitPrice = ei.insume.price,
                    totalPrice = ei.insume.price.multiply(BigDecimal.valueOf(ei.quantity.toLong()))
                )
            },
            totalPrice = it.getTotalPrice(),
            status = it.status.name
        ) 
    },
    history = histories.sortedBy { it.registerTime }.map { it.toServiceOrderHistoryEntry() }
)

fun ServiceOrderHistory.toServiceOrderHistoryEntry() = ServiceOrderHistoryEntry(
    id = id,
    status = status,
    registerTime = registerTime,
    intervalTime = intervalTime
)
