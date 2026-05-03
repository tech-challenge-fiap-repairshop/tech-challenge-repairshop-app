package com.cao.repairshop.register.controller

import com.cao.repairshop.register.controller.interfaces.CustomerApi
import com.cao.repairshop.register.dto.CreateCustomerRequest
import com.cao.repairshop.register.dto.CustomerResponse
import com.cao.repairshop.register.dto.UpdateCustomerRequest
import com.cao.repairshop.register.mapper.toResponse
import com.cao.repairshop.register.service.CustomerService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/customers")
class CustomerController(
    private val customerService: CustomerService
) : CustomerApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(@Valid @RequestBody request: CreateCustomerRequest): CustomerResponse =
        customerService.create(request).toResponse()

    @GetMapping
    override fun findAll(pageable: Pageable): Page<CustomerResponse> =
        customerService.findAll(pageable).map { it.toResponse() }

    @GetMapping("/{id}")
    override fun findById(@PathVariable id: UUID): CustomerResponse =
        customerService.findById(id).toResponse()

    @PutMapping("/{id}")
    override fun update(@PathVariable id: UUID, @Valid @RequestBody request: UpdateCustomerRequest): CustomerResponse =
        customerService.update(id, request).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun delete(@PathVariable id: UUID) = customerService.delete(id)
}
