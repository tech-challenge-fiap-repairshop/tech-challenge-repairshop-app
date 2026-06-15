package com.cao.repairshop.execution.domain.entities.mapper

import com.cao.repairshop.execution.domain.entities.Execution
import com.cao.repairshop.execution.domain.entities.ExecutionHistory
import com.cao.repairshop.execution.domain.entities.ExecutionInsume
import com.cao.repairshop.execution.infra.persistence.models.ExecutionEntity
import com.cao.repairshop.execution.infra.persistence.models.ExecutionHistoryEntity
import com.cao.repairshop.execution.infra.persistence.models.ExecutionInsumeEntity
import com.cao.repairshop.execution.infra.persistence.models.ExecutionInsumeId
import com.cao.repairshop.execution.infra.controller.dtos.ExecutionHistoryResponse
import com.cao.repairshop.execution.infra.controller.dtos.ExecutionResponse
import com.cao.repairshop.inventory.domain.entities.mapper.toDomain
import com.cao.repairshop.inventory.domain.entities.mapper.toEntity
import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import com.cao.repairshop.serviceorder.infra.controller.dtos.InsumeSummaryResponse
import java.math.BigDecimal

fun Execution.toResponse() = ExecutionResponse(
    id = id,
    serviceOrderId = serviceOrderId,
    basicDescription = basicDescription,
    fullDescription = fullDescription,
    laborPrice = price,
    insumes = insumes.map { ei ->
        InsumeSummaryResponse(
            name = ei.insume.name,
            quantity = ei.quantity,
            unitPrice = ei.insume.price,
            totalPrice = ei.insume.price.multiply(BigDecimal.valueOf(ei.quantity.toLong()))
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
    executionId = executionId,
    status = status,
    registerTime = registerTime,
    intervalTime = intervalTime
)

fun Execution.toEntity(serviceOrderEntity: ServiceOrderEntity): ExecutionEntity {
    val executionEntity = ExecutionEntity(
        id = id,
        serviceOrder = serviceOrderEntity,
        basicDescription = basicDescription,
        fullDescription = fullDescription,
        price = price,
        estimatedTime = estimatedTime,
        status = status,
        created = created,
        updated = updated
    )
    executionEntity.histories = histories.map { it.toEntity(executionEntity) }.toMutableSet()
    executionEntity.insumes = insumes.map { it.toEntity(executionEntity) }.toMutableSet()
    return executionEntity
}

fun ExecutionEntity.toDomain(): Execution {
    val execution = Execution(
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
    execution.histories = histories.map { it.toDomain() }.toMutableSet()
    execution.insumes = insumes.map { it.toDomain() }.toMutableSet()
    return execution
}

fun ExecutionHistory.toEntity(executionEntity: ExecutionEntity) = ExecutionHistoryEntity(
    id = id,
    execution = executionEntity,
    status = status,
    description = description,
    registerTime = registerTime,
    intervalTime = intervalTime
)

fun ExecutionHistoryEntity.toDomain() = ExecutionHistory(
    id = id,
    executionId = execution.id,
    status = status,
    description = description,
    registerTime = registerTime,
    intervalTime = intervalTime
)

fun ExecutionInsume.toEntity(executionEntity: ExecutionEntity) = ExecutionInsumeEntity(
    id = ExecutionInsumeId(executionEntity.id, insume.id),
    execution = executionEntity,
    insume = insume.toEntity(),
    quantity = quantity
)

fun ExecutionInsumeEntity.toDomain() = ExecutionInsume(
    executionId = execution.id,
    insume = insume.toDomain(),
    quantity = quantity
)
