package com.cao.repairshop.serviceorder.application.usecases.impl.strategies

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder

sealed interface EmailStrategy {
    fun formatSubject(order: ServiceOrder): String
    fun formatBody(customer: Customer, order: ServiceOrder, vehicle: Vehicle): String
}
