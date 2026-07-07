package com.cao.repairshop.serviceorder.infra.gateways

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.register.infra.persistence.repositories.CustomerRepository
import com.cao.repairshop.register.infra.persistence.repositories.VehicleRepository
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import com.cao.repairshop.serviceorder.domain.entities.mapper.toDomain
import com.cao.repairshop.serviceorder.domain.entities.mapper.toEntity
import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import com.cao.repairshop.serviceorder.infra.persistence.repositories.ServiceOrderRepository
import com.cao.repairshop.serviceorder.infra.persistence.repositories.ServiceOrderSpecifications
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.util.*

@Component
class ServiceOrderGatewayImplJPA(
    private val serviceOrderRepository: ServiceOrderRepository,
    private val customerRepository: CustomerRepository,
    private val vehicleRepository: VehicleRepository
) : ServiceOrderGateway {

    override fun save(order: ServiceOrder): ServiceOrder {
        val customerEntity = customerRepository.findById(order.customerId)
            .orElseThrow { EntityNotFoundException("Customer not found with ID ${order.customerId}") }
        val vehicleEntity = vehicleRepository.findById(order.vehicleId)
            .orElseThrow { EntityNotFoundException("Vehicle not found with ID ${order.vehicleId}") }
        val entity = order.toEntity(customerEntity, vehicleEntity)
        val saved = serviceOrderRepository.save(entity)
        return saved.toDomain()
    }

    override fun findDetailedById(id: UUID): ServiceOrder? {
        return serviceOrderRepository.findDetailedById(id).orElse(null)?.toDomain()
    }

    override fun findAll(spec: Specification<ServiceOrderEntity>?, pageable: Pageable): Page<ServiceOrder> {
        val finalSpec = ServiceOrderSpecifications.withCustomOrderingAndFilters(spec, pageable)
        return serviceOrderRepository.findAll(finalSpec, pageable).map { it.toDomain() }
    }

    override fun existsByCustomerId(customerId: UUID): Boolean {
        return serviceOrderRepository.existsByCustomerId(customerId)
    }

    override fun existsByVehicleId(vehicleId: UUID): Boolean {
        return serviceOrderRepository.existsByVehicleId(vehicleId)
    }

    override fun getAverageExecutionTimeMinutes(): Double? {
        return serviceOrderRepository.getAverageExecutionTimeMinutes()
    }
}