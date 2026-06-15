package com.cao.repairshop.serviceorder.infra.gateways

import com.cao.repairshop.register.domain.ServiceOrderExistenceChecker
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ServiceOrderExistenceAdapter(
    private val serviceOrderGateway: ServiceOrderGateway
) : ServiceOrderExistenceChecker {

    override fun existsByCustomerId(customerId: UUID): Boolean =
        serviceOrderGateway.existsByCustomerId(customerId)

    override fun existsByVehicleId(vehicleId: UUID): Boolean =
        serviceOrderGateway.existsByVehicleId(vehicleId)
}
