package com.cao.repairshop.serviceorder.application.gateways

import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ServiceOrderGateway {
    fun save(order: ServiceOrder): ServiceOrder
    fun findDetailedById(id: UUID): ServiceOrder?
    fun findAll(
        customerId: UUID?,
        vehicleId: UUID?,
        status: ServiceOrderStatus?,
        pageable: Pageable
    ): Page<ServiceOrder>
    fun existsByCustomerId(customerId: UUID): Boolean
    fun existsByVehicleId(vehicleId: UUID): Boolean
    fun getAverageExecutionTimeMinutes(): Double?
}
