package com.cao.repairshop.serviceorder.service

import com.cao.repairshop.register.domain.ServiceOrderExistenceChecker
import com.cao.repairshop.serviceorder.repository.ServiceOrderRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ServiceOrderExistenceAdapter(
    private val serviceOrderRepository: ServiceOrderRepository
) : ServiceOrderExistenceChecker {

    override fun existsByCustomerId(customerId: UUID): Boolean =
        serviceOrderRepository.existsByCustomerId(customerId)

    override fun existsByVehicleId(vehicleId: UUID): Boolean =
        serviceOrderRepository.existsByVehicleId(vehicleId)
}
