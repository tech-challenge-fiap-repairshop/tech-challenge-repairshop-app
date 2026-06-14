package com.cao.repairshop.inventory.application.usecases.impl

import com.cao.repairshop.inventory.application.gateways.InsumeGateway
import com.cao.repairshop.inventory.application.usecases.FindAllInsumes
import com.cao.repairshop.inventory.domain.entities.mapper.toResponse
import com.cao.repairshop.inventory.infra.controller.dtos.InsumeResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FindAllInsumesImpl(
    private val insumeGateway: InsumeGateway
) : FindAllInsumes {
    @Transactional(readOnly = true)
    override fun execute(pageable: Pageable): Page<InsumeResponse> {
        return insumeGateway.findAll(pageable).map { it.toResponse() }
    }
}

