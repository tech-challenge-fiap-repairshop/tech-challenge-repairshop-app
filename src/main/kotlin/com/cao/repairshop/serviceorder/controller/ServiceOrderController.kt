package com.cao.repairshop.serviceorder.controller

import com.cao.repairshop.execution.dto.ExecutionMetricsResponse
import com.cao.repairshop.serviceorder.controller.interfaces.ServiceOrderApi
import com.cao.repairshop.serviceorder.dto.*
import com.cao.repairshop.serviceorder.mapper.toResponse
import com.cao.repairshop.serviceorder.service.ServiceOrderMetricsService
import com.cao.repairshop.serviceorder.service.ServiceOrderService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/service-orders")
class ServiceOrderController(
    private val serviceOrderService: ServiceOrderService,
    private val serviceOrderMetricsService: ServiceOrderMetricsService
) : ServiceOrderApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(@Valid @RequestBody request: CreateServiceOrderRequest): ServiceOrderResponse =
        serviceOrderService.createServiceOrder(request).toResponse()

    @GetMapping
    override fun findAll(pageable: Pageable): Page<ServiceOrderResponse> =
        serviceOrderService.findAll(pageable).map { it.toResponse() }

    @GetMapping("/{id}")
    override fun findById(@PathVariable id: UUID): ServiceOrderResponse =
        serviceOrderService.findServiceOrder(id).toResponse()

    @PatchMapping("/{id}/status")
    override fun advanceStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ServiceOrderStatusUpdateRequest
    ): ServiceOrderResponse =
        serviceOrderService.advanceStatus(id, request.status).toResponse()

    @PostMapping("/{id}/approve")
    override fun approve(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ApprovalRequest
    ): ServiceOrderResponse =
        serviceOrderService.approve(id, request).toResponse()

    @GetMapping("/metrics")
    override fun getMetrics(): ServiceOrderMetricsResponse =
        serviceOrderMetricsService.getMetrics()

    @GetMapping("/executions/metrics")
    override fun getExecutionMetrics(): ExecutionMetricsResponse =
        serviceOrderMetricsService.getExecutionMetrics()
}
