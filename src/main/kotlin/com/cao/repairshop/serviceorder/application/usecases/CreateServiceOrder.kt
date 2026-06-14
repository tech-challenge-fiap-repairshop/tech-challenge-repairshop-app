package com.cao.repairshop.serviceorder.application.usecases

import com.cao.repairshop.serviceorder.infra.controller.dtos.CreateServiceOrderRequest
import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import java.util.UUID

interface CreateServiceOrder {
    fun execute(request: CreateServiceOrderRequest): ServiceOrderResponse
}
