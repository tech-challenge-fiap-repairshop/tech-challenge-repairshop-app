package com.cao.repairshop.inventory.application.usecases

import com.cao.repairshop.inventory.infra.controller.dtos.InsumeResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface FindAllInsumes {
    fun execute(pageable: Pageable): Page<InsumeResponse>
}

