package com.cao.repairshop.execution.controller

import com.cao.repairshop.execution.dto.CreateExecutionBatchRequest
import com.cao.repairshop.execution.dto.CreateExecutionRequest
import com.cao.repairshop.execution.dto.UpdateExecutionRequest
import com.cao.repairshop.execution.dto.ExecutionStatusUpdateRequest
import com.cao.repairshop.execution.dto.ExecutionResponse
import com.cao.repairshop.serviceorder.service.ServiceOrderService

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/service-orders/{serviceOrderId}/executions")
@Tag(name = "Executions", description = "Service execution management within service orders")
class ExecutionController(
    private val serviceOrderService: ServiceOrderService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new execution to a service order")
    fun create(
        @PathVariable serviceOrderId: UUID,
        @Valid @RequestBody request: CreateExecutionRequest
    ): ExecutionResponse = serviceOrderService.addExecution(serviceOrderId, request)

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add multiple executions in batch")
    fun createBatch(
        @PathVariable serviceOrderId: UUID,
        @Valid @RequestBody request: CreateExecutionBatchRequest
    ): List<ExecutionResponse> = serviceOrderService.addExecutionBatch(serviceOrderId, request)

    @GetMapping("/{executionId}")
    @Operation(summary = "Find execution by ID within a service order")
    fun findById(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID
    ): ExecutionResponse = serviceOrderService.findExecution(serviceOrderId, executionId)

    @PutMapping("/{executionId}")
    @Operation(summary = "Update execution details")
    fun update(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID,
        @Valid @RequestBody request: UpdateExecutionRequest
    ): ExecutionResponse = serviceOrderService.updateExecution(serviceOrderId, executionId, request)

    @DeleteMapping("/{executionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove an execution from a service order")
    fun delete(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID
    ) = serviceOrderService.removeExecution(serviceOrderId, executionId)

    @PatchMapping("/{executionId}/status")
    @Operation(summary = "Advance execution status")
    fun advanceStatus(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID,
        @Valid @RequestBody request: ExecutionStatusUpdateRequest
    ): ExecutionResponse = serviceOrderService.advanceExecutionStatus(serviceOrderId, executionId, request.status)
}
