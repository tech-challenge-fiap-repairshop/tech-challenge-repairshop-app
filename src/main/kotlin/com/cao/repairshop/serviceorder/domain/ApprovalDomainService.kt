package com.cao.repairshop.serviceorder.domain

import com.cao.repairshop.serviceorder.entity.ServiceOrder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ApprovalDomainService {

    data class StockRequirement(val insumeId: UUID, val quantity: Int)

    fun approve(order: ServiceOrder): List<StockRequirement> {
        order.approve()
        order.advanceStatus(ServiceOrderStatus.IN_EXECUTION)
        return order.collectInsumeRequirements()
            .map { (id, qty) -> StockRequirement(id, qty) }
    }

    fun refuse(order: ServiceOrder) {
        order.refuse()
    }
}
