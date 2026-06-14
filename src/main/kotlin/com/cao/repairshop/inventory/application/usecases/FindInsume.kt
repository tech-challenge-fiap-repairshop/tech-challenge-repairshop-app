package com.cao.repairshop.inventory.application.usecases

import com.cao.repairshop.inventory.domain.entities.Insume
import com.cao.repairshop.inventory.infra.controller.dtos.InsumeResponse
import java.util.UUID

interface FindInsume {
    fun execute(id: UUID): InsumeResponse
    fun getEntityById(id: UUID): Insume
}

