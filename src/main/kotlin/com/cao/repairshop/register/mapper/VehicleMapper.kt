package com.cao.repairshop.register.mapper

import com.cao.repairshop.register.dto.VehicleResponse
import com.cao.repairshop.register.entity.Vehicle

fun Vehicle.toResponse() = VehicleResponse(
    id = id,
    customerId = customer.id,
    plate = plate.normalized,
    brand = brand,
    model = model,
    color = color,
    manufacturingDate = manufacturingDate,
    lastMaintenance = lastMaintenance,
    created = created,
    updated = updated
)

fun List<Vehicle>.toResponse() = map { it.toResponse() }