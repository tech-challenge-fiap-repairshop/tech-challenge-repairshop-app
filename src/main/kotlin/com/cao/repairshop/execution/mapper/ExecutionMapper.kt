package com.cao.repairshop.execution.mapper

import java.util.UUID

import com.cao.repairshop.execution.dto.ExecutionHistoryResponse
import com.cao.repairshop.execution.dto.ExecutionResponse
import com.cao.repairshop.execution.entity.Execution
import com.cao.repairshop.execution.entity.ExecutionHistory

fun Execution.toResponse() = ExecutionResponse(
    id = id,
    serviceOrderId = serviceOrder.id,
    basicDescription = basicDescription,
    fullDescription = fullDescription,
    price = price,
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
