package com.cao.repairshop.serviceorder.application.usecases

import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus

fun interface NotifyCustomer {
    fun execute(order: ServiceOrder, status: ServiceOrderStatus)
}
