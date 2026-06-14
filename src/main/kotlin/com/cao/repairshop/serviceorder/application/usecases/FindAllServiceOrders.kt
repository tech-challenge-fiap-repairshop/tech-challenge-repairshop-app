package com.cao.repairshop.serviceorder.application.usecases

import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface FindAllServiceOrders {
    fun execute(pageable: Pageable): Page<ServiceOrderResponse>
}
