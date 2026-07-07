package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.application.usecases.AdvanceServiceOrderStatus
import com.cao.repairshop.serviceorder.application.usecases.NotifyCustomer
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.mapper.toResponse
import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AdvanceServiceOrderStatusImpl(
    private val serviceOrderGateway: ServiceOrderGateway,
    private val notifyCustomer: NotifyCustomer
) : AdvanceServiceOrderStatus {

    @Transactional
    override fun execute(id: UUID, newStatus: ServiceOrderStatus): ServiceOrderResponse {
        val order = serviceOrderGateway.findDetailedById(id)
            ?: throw EntityNotFoundException(ErrorMessages.ServiceOrder.NOT_FOUND)

        order.advanceStatus(newStatus)
        val saved = serviceOrderGateway.save(order)

        notifyCustomer.execute(saved, newStatus)

        return saved.toResponse()
    }
}