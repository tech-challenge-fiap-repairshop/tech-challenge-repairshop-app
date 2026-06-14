package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.application.usecases.FindServiceOrder
import com.cao.repairshop.serviceorder.domain.entities.mapper.toResponse
import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FindServiceOrderImpl(
    private val serviceOrderGateway: ServiceOrderGateway
) : FindServiceOrder {

    @Transactional(readOnly = true)
    override fun execute(id: UUID): ServiceOrderResponse {
        val order = serviceOrderGateway.findDetailedById(id)
            ?: throw EntityNotFoundException(ErrorMessages.ServiceOrder.NOT_FOUND)
        return order.toResponse()
    }
}
