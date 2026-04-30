package com.cao.repairshop.execution.mapper

import java.util.UUID

import com.cao.repairshop.execution.dto.ExecutionHistoryResponse
import com.cao.repairshop.execution.dto.ExecutionResponse
import com.cao.repairshop.serviceorder.dto.InsumeSummaryResponse
import com.cao.repairshop.execution.entity.Execution
import com.cao.repairshop.execution.entity.ExecutionHistory

fun Execution.toResponse() = ExecutionResponse(
    id = id,
    serviceOrderId = serviceOrder.id,
    basicDescription = basicDescription,
    fullDescription = fullDescription,
    laborPrice = price,
    insumes = insumes.map { ei ->
        InsumeSummaryResponse(
            name = ei.insume.name,
            quantity = ei.quantity,
            unitPrice = ei.insume.price,
            totalPrice = ei.insume.price.multiply(java.math.BigDecimal.valueOf(ei.quantity.toLong()))
        )
    },
    totalPrice = getTotalPrice(),
    estimatedTime = estimatedTime,
    status = status,
    created = created,
    updated = updated
)

fun ExecutionHistory.toResponse() = ExecutionHistoryResponse(
    id = id,
    executionId = execution.id,
    status = status,
    registerTime = registerTime,
    intervalTime = intervalTime
)
