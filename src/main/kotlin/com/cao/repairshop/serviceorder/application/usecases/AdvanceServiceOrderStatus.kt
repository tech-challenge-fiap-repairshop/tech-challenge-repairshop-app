package com.cao.repairshop.serviceorder.application.usecases

import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import java.util.UUID

fun interface AdvanceServiceOrderStatus {
    fun execute(id: UUID, newStatus: ServiceOrderStatus): ServiceOrderResponse
}
