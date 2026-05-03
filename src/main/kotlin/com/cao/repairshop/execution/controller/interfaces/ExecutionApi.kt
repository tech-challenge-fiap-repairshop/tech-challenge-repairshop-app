package com.cao.repairshop.execution.controller.interfaces

import com.cao.repairshop.execution.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID

@Tag(name = "Executions", description = "Service execution management within service orders")
interface ExecutionApi {

    @Operation(summary = "Add a new execution to a service order")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Execution added successfully",
            content = [Content(schema = Schema(implementation = ExecutionResponse::class))]),
        ApiResponse(responseCode = "400", description = "Validation error", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Service order or insume not found", content = [Content()])
    )
    fun create(serviceOrderId: UUID, request: CreateExecutionRequest): ExecutionResponse

    @Operation(summary = "Add multiple executions in batch")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Executions added successfully"),
        ApiResponse(responseCode = "400", description = "Validation error", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Service order or insume not found", content = [Content()])
    )
    fun createBatch(serviceOrderId: UUID, request: CreateExecutionBatchRequest): List<ExecutionResponse>

    @Operation(summary = "Find execution by ID within a service order")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Execution found",
            content = [Content(schema = Schema(implementation = ExecutionResponse::class))]),
        ApiResponse(responseCode = "404", description = "Execution or service order not found", content = [Content()])
    )
    fun findById(serviceOrderId: UUID, executionId: UUID): ExecutionResponse

    @Operation(summary = "Update execution details")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Execution updated",
            content = [Content(schema = Schema(implementation = ExecutionResponse::class))]),
        ApiResponse(responseCode = "400", description = "Validation error", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Execution not found", content = [Content()])
    )
    fun update(serviceOrderId: UUID, executionId: UUID, request: UpdateExecutionRequest): ExecutionResponse

    @Operation(summary = "Remove an execution from a service order")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Execution removed", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Execution not found", content = [Content()])
    )
    fun delete(serviceOrderId: UUID, executionId: UUID)

    @Operation(summary = "Advance execution status")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Status advanced successfully",
            content = [Content(schema = Schema(implementation = ExecutionResponse::class))]),
        ApiResponse(responseCode = "400", description = "Invalid status transition", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Execution not found", content = [Content()])
    )
    fun advanceStatus(serviceOrderId: UUID, executionId: UUID, request: ExecutionStatusUpdateRequest): ExecutionResponse
}
