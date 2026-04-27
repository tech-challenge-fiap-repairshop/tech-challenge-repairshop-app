package com.cao.repairshop.register.repository

import com.cao.repairshop.register.entity.Vehicle
import com.cao.repairshop.register.domain.Plate

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface VehicleRepository : JpaRepository<Vehicle, UUID> {
    fun findByPlate(plate: Plate): Vehicle?
}
