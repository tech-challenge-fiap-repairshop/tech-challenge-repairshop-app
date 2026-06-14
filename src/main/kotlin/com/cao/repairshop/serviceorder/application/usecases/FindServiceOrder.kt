package com.cao.repairshop.serviceorder.application.usecases

import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import java.util.UUID

interface FindServiceOrder {
    fun execute(id: UUID): ServiceOrderResponse
}
