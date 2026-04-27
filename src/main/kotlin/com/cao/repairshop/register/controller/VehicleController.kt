package com.cao.repairshop.register.controller

import com.cao.repairshop.register.service.VehicleService
import com.cao.repairshop.register.dto.CreateVehicleRequest
import com.cao.repairshop.register.dto.UpdateVehicleRequest
import com.cao.repairshop.register.dto.VehicleResponse
import com.cao.repairshop.register.mapper.toResponse

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/vehicles")
class VehicleController(
    private val vehicleService: VehicleService
) : com.cao.repairshop.register.controller.interfaces.VehicleApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(@Valid @RequestBody request: CreateVehicleRequest): VehicleResponse =
        vehicleService.create(request).toResponse()

    @GetMapping
    override fun findAll(pageable: Pageable): Page<VehicleResponse> =
        vehicleService.findAll(pageable).map { it.toResponse() }

    @GetMapping("/{id}")
    override fun findById(@PathVariable id: UUID): VehicleResponse =
        vehicleService.findById(id).toResponse()

    @PutMapping("/{id}")
    override fun update(@PathVariable id: UUID, @Valid @RequestBody request: UpdateVehicleRequest): VehicleResponse =
        vehicleService.update(id, request).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun delete(@PathVariable id: UUID) = vehicleService.delete(id)
}
