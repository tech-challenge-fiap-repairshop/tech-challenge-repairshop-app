package com.cao.repairshop.register.service

import com.cao.repairshop.core.exception.BusinessRuleViolationException
import com.cao.repairshop.core.exception.DuplicateEntityException
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.register.domain.Email
import com.cao.repairshop.register.domain.Plate
import com.cao.repairshop.register.domain.ServiceOrderExistenceChecker
import com.cao.repairshop.register.dto.CreateVehicleRequest
import com.cao.repairshop.register.dto.UpdateVehicleRequest
import com.cao.repairshop.register.entity.Vehicle
import com.cao.repairshop.register.repository.VehicleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class VehicleService(
    private val vehicleRepository: VehicleRepository,
    private val customerService: CustomerService,
    private val serviceOrderExistenceChecker: ServiceOrderExistenceChecker,
) {

    @Transactional
    fun create(request: CreateVehicleRequest): Vehicle {
        val plate = Plate(request.plate)
        val email = Email(request.customerEmail)

        vehicleRepository.findByPlate(plate)?.let {
            throw DuplicateEntityException(ErrorMessages.Vehicle.DUPLICATE_PLATE)
        }

        val customer = customerService.findByEmailOrThrow(email)

        val vehicle = Vehicle(
            customer = customer,
            plate = plate,
            brand = request.brand,
            model = request.model,
            color = request.color,
            manufacturingDate = request.manufacturingDate
        )
        return vehicleRepository.save(vehicle)
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): Vehicle {
        return vehicleRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorMessages.Vehicle.NOT_FOUND) }
    }

    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<Vehicle> =
        vehicleRepository.findAll(pageable)

    @Transactional
    fun update(id: UUID, request: UpdateVehicleRequest): Vehicle {
        val vehicle = vehicleRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorMessages.Vehicle.NOT_FOUND) }

        val plate = Plate(request.plate)

        vehicleRepository.findByPlate(plate)?.let { existingVehicle ->
            if (existingVehicle.id != id) {
                throw DuplicateEntityException(ErrorMessages.Vehicle.DUPLICATE_PLATE)
            }
        }
        vehicle.updateDetails(
            plate = plate,
            brand = request.brand,
            model = request.model,
            color = request.color,
            manufacturingDate = request.manufacturingDate
        )

        return vehicleRepository.save(vehicle)
    }

    @Transactional(readOnly = true)
    fun verifyAndTakeByPlate(plate: Plate): Vehicle =
        vehicleRepository.findByPlate(plate)
            ?: throw EntityNotFoundException(ErrorMessages.Vehicle.NOT_FOUND)

    @Transactional
    fun delete(id: UUID) {
        val vehicle =
            vehicleRepository.findById(id).orElseThrow { EntityNotFoundException(ErrorMessages.Vehicle.NOT_FOUND) }

        if (serviceOrderExistenceChecker.existsByVehicleId(vehicle.id))
            throw BusinessRuleViolationException(ErrorMessages.Vehicle.HAS_SERVICE_ORDERS)

        vehicleRepository.delete(vehicle)
    }
}
