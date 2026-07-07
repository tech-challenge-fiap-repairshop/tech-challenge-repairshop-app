package com.cao.repairshop.serviceorder.application.gateways

import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.util.UUID

interface ServiceOrderGateway {
    fun save(order: ServiceOrder): ServiceOrder
    fun findDetailedById(id: UUID): ServiceOrder?
    fun findAll(spec: Specification<ServiceOrderEntity>?, pageable: Pageable): Page<ServiceOrder>
    fun existsByCustomerId(customerId: UUID): Boolean
    fun existsByVehicleId(vehicleId: UUID): Boolean
    fun getAverageExecutionTimeMinutes(): Double?
}
