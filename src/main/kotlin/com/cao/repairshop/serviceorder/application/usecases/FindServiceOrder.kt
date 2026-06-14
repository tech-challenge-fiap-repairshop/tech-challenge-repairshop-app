package com.cao.repairshop.serviceorder.application.usecases

import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface FindServiceOrder {
    fun findById(id: UUID): ServiceOrderResponse
    fun findAll(pageable: Pageable): Page<ServiceOrderResponse>
}

