package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.inventory.application.usecases.DeductInsumeStock
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.application.usecases.ApproveServiceOrder
import com.cao.repairshop.serviceorder.application.usecases.NotifyCustomer
import com.cao.repairshop.serviceorder.domain.ApprovalDomainService
import com.cao.repairshop.serviceorder.domain.entities.mapper.toResponse
import com.cao.repairshop.serviceorder.infra.controller.dtos.ApprovalRequest
import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ApproveServiceOrderImpl(
    private val serviceOrderGateway: ServiceOrderGateway,
    private val deductInsumeStock: DeductInsumeStock,
    private val approvalDomainService: ApprovalDomainService,
    private val notifyCustomer: NotifyCustomer
) : ApproveServiceOrder {

    @Transactional
    override fun execute(id: UUID, request: ApprovalRequest): ServiceOrderResponse {
        val order = serviceOrderGateway.findDetailedById(id)
            ?: throw EntityNotFoundException(ErrorMessages.ServiceOrder.NOT_FOUND)

        if (request.approved) {
            val stockRequirements = approvalDomainService.approve(order)
            stockRequirements.forEach { req ->
                deductInsumeStock.execute(req.insumeId, req.quantity)
            }
        } else {
            approvalDomainService.refuse(order)
        }

        val saved = serviceOrderGateway.save(order)
        notifyCustomer.execute(saved, saved.status)
        return saved.toResponse()
    }
}
