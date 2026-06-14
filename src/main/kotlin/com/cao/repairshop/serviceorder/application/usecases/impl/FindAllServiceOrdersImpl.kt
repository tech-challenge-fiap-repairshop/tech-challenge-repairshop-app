package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.application.usecases.FindAllServiceOrders
import com.cao.repairshop.serviceorder.domain.entities.mapper.toResponse
import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FindAllServiceOrdersImpl(
    private val serviceOrderGateway: ServiceOrderGateway
) : FindAllServiceOrders {

    @Transactional(readOnly = true)
    override fun execute(pageable: Pageable): Page<ServiceOrderResponse> {
        return serviceOrderGateway.findAll(pageable).map { it.toResponse() }
    }
}
