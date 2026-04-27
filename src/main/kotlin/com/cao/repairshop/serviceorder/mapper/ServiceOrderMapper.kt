package com.cao.repairshop.serviceorder.mapper
 
import java.time.LocalDateTime
import java.util.UUID

import com.cao.repairshop.serviceorder.dto.ServiceOrderHistoryEntry
import com.cao.repairshop.serviceorder.dto.ServiceOrderResponse
import com.cao.repairshop.serviceorder.dto.ExecutionSummary
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import com.cao.repairshop.serviceorder.entity.ServiceOrderHistory

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
            price = it.price,
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
