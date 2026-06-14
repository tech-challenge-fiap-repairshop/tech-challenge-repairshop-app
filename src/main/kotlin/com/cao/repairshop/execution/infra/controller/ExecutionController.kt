package com.cao.repairshop.execution.infra.controller

import com.cao.repairshop.execution.application.usecases.*
import com.cao.repairshop.execution.infra.controller.dtos.*
import com.cao.repairshop.execution.infra.controller.interfaces.ExecutionApi
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/service-orders/{serviceOrderId}/executions")
class ExecutionController(
    private val addExecution: AddExecution,
    private val addExecutionBatch: AddExecutionBatch,
    private val findExecution: FindExecution,
    private val updateExecution: UpdateExecution,
    private val removeExecution: RemoveExecution,
    private val advanceExecutionStatus: AdvanceExecutionStatus
) : ExecutionApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(
        @PathVariable serviceOrderId: UUID,
        @RequestBody request: CreateExecutionRequest
    ): ExecutionResponse = addExecution.execute(serviceOrderId, request)

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    override fun createBatch(
        @PathVariable serviceOrderId: UUID,
        @RequestBody request: CreateExecutionBatchRequest
    ): List<ExecutionResponse> = addExecutionBatch.execute(serviceOrderId, request)

    @GetMapping("/{executionId}")
    override fun findById(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID
    ): ExecutionResponse = findExecution.findById(serviceOrderId, executionId)

    @PutMapping("/{executionId}")
    override fun update(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID,
        @RequestBody request: UpdateExecutionRequest
    ): ExecutionResponse = updateExecution.execute(serviceOrderId, executionId, request)

    @DeleteMapping("/{executionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun delete(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID
    ) = removeExecution.execute(serviceOrderId, executionId)

    @PatchMapping("/{executionId}/status")
    override fun advanceStatus(
        @PathVariable serviceOrderId: UUID,
        @PathVariable executionId: UUID,
        @RequestBody request: ExecutionStatusUpdateRequest
    ): ExecutionResponse = advanceExecutionStatus.execute(serviceOrderId, executionId, request.status)
}
