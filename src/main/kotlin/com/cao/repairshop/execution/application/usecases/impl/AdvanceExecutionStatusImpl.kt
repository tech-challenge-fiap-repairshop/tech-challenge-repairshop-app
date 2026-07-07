package com.cao.repairshop.execution.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.execution.application.usecases.AdvanceExecutionStatus
import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.execution.domain.entities.mapper.toResponse
import com.cao.repairshop.execution.infra.controller.dtos.ExecutionResponse
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.application.usecases.NotifyCustomer
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdvanceExecutionStatusImpl(
    private val serviceOrderGateway: ServiceOrderGateway,
    private val notifyCustomer: NotifyCustomer
) : AdvanceExecutionStatus {

    @Transactional
    override fun execute(serviceOrderId: UUID, executionId: UUID, newStatus: ExecutionStatus): ExecutionResponse {
        val order = serviceOrderGateway.findDetailedById(serviceOrderId)
            ?: throw EntityNotFoundException(ErrorMessages.ServiceOrder.NOT_FOUND)

        order.ensureNotTerminalState("advance execution status")

        val execution = order.executions.find { it.id == executionId }
            ?: throw EntityNotFoundException(ErrorMessages.Execution.NOT_FOUND)

        execution.advanceStatus(newStatus)
        val transitioned = order.checkCompletion()

        val saved = serviceOrderGateway.save(order)
        if (transitioned) {
            notifyCustomer.execute(saved, ServiceOrderStatus.FINALIZED)
        }
        return execution.toResponse()
    }
}
