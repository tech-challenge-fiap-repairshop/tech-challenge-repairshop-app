package com.cao.repairshop.execution.controller

import com.cao.repairshop.execution.controller.interfaces.ExecutionApi
import com.cao.repairshop.execution.dto.*
import com.cao.repairshop.serviceorder.service.ServiceOrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/service-orders/{serviceOrderId}/executions")
class ExecutionController(
    private val serviceOrderService: ServiceOrderService
) : ExecutionApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(
        @PathVariable serviceOrderId: UUID,
        @Valid @RequestBody request: CreateExecutionRequest
    ): ExecutionResponse = serviceOrderService.addExecution(serviceOrderId, request)

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    override fun createBatch(
        @PathVariable serviceOrderId: UUID,
        @Valid @RequestBody request: CreateExecutionBatchRequest
    ): List<ExecutionResponse> = serviceOrderService.addExecutionBatch(serviceOrderId, request)

    @GetMapping("/{executionId}")
    override fun findById(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID
    ): ExecutionResponse = serviceOrderService.findExecution(serviceOrderId, executionId)

    @PutMapping("/{executionId}")
    override fun update(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID,
        @Valid @RequestBody request: UpdateExecutionRequest
    ): ExecutionResponse = serviceOrderService.updateExecution(serviceOrderId, executionId, request)

    @DeleteMapping("/{executionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun delete(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID
    ) = serviceOrderService.removeExecution(serviceOrderId, executionId)

    @PatchMapping("/{executionId}/status")
    override fun advanceStatus(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID,
        @Valid @RequestBody request: ExecutionStatusUpdateRequest
    ): ExecutionResponse = serviceOrderService.advanceExecutionStatus(serviceOrderId, executionId, request.status)
}
