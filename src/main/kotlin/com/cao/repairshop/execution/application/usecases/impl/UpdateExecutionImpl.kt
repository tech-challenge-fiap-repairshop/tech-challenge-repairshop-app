package com.cao.repairshop.execution.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.execution.application.usecases.UpdateExecution
import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.execution.domain.entities.mapper.toResponse
import com.cao.repairshop.execution.infra.controller.dtos.ExecutionResponse
import com.cao.repairshop.execution.infra.controller.dtos.UpdateExecutionRequest
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class UpdateExecutionImpl(
    private val serviceOrderGateway: ServiceOrderGateway
) : UpdateExecution {

    @Transactional
    override fun execute(serviceOrderId: UUID, executionId: UUID, request: UpdateExecutionRequest): ExecutionResponse {
        val order = serviceOrderGateway.findDetailedById(serviceOrderId)
            ?: throw EntityNotFoundException(ErrorMessages.ServiceOrder.NOT_FOUND)

        val execution = order.executions.find { it.id == executionId }
            ?: throw EntityNotFoundException(ErrorMessages.Execution.NOT_FOUND)

        execution.apply {
            basicDescription = BasicExecution.valueOf(request.basicDescription.uppercase())
            fullDescription = request.fullDescription
            price = request.price
            estimatedTime = request.estimatedTime
            updated = LocalDateTime.now()
            recordHistory(status, "Execution attributes updated (price, description or time)")
        }

        order.recalculateTotalPrice()
        serviceOrderGateway.save(order)
        return execution.toResponse()
    }
}
