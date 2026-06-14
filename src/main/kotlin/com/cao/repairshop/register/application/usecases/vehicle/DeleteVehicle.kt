package com.cao.repairshop.register.application.usecases.vehicle

import java.util.UUID

interface DeleteVehicle {
    fun execute(id: UUID)
}
