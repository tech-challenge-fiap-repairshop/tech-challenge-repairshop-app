package com.cao.repairshop.serviceorder.application.usecases

import com.cao.repairshop.serviceorder.infra.controller.dtos.ApprovalRequest
import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import java.util.UUID

interface ApproveServiceOrder {
    fun execute(id: UUID, request: ApprovalRequest): ServiceOrderResponse
}
