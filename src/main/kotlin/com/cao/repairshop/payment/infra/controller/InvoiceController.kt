package com.cao.repairshop.payment.infra.controller

import com.cao.repairshop.payment.infra.controller.interfaces.InvoiceApi
import com.cao.repairshop.payment.application.usecases.*
import com.cao.repairshop.payment.infra.controller.dtos.*
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/invoices")
class InvoiceController(
    private val createInvoice: CreateInvoice,
    private val findInvoice: FindInvoice
) : InvoiceApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(@Valid @RequestBody request: CreateInvoiceRequest): InvoiceResponse =
        createInvoice.execute(request)

    @GetMapping("/{id}")
    override fun findById(@PathVariable id: UUID): InvoiceResponse =
        findInvoice.findById(id)

    @GetMapping
    override fun findAll(pageable: Pageable): Page<InvoiceResponse> =
        findInvoice.findAll(pageable)
}

