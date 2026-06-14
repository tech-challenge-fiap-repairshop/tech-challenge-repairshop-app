package com.cao.repairshop.inventory.application.usecases

import com.cao.repairshop.inventory.infra.controller.dtos.CreateInsumeRequest
import com.cao.repairshop.inventory.infra.controller.dtos.InsumeResponse

interface CreateInsume {
    fun execute(request: CreateInsumeRequest): InsumeResponse
}

