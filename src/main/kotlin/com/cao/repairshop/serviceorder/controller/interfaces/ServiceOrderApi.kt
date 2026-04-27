package com.cao.repairshop.serviceorder.controller.interfaces

import com.cao.repairshop.serviceorder.dto.ApprovalRequest
import com.cao.repairshop.serviceorder.dto.CreateServiceOrderRequest
import com.cao.repairshop.serviceorder.dto.ServiceOrderMetricsResponse
import com.cao.repairshop.serviceorder.dto.ServiceOrderStatusUpdateRequest
import com.cao.repairshop.serviceorder.dto.ServiceOrderResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

@Tag(name = "Service Orders", description = "Service order lifecycle management")
interface ServiceOrderApi {

    @Operation(summary = "Open a new service order")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Service order created",
            content = [Content(schema = Schema(implementation = ServiceOrderResponse::class))]),
        ApiResponse(responseCode = "400", description = "Validation error", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Customer or vehicle not found", content = [Content()])
    )
    fun create(request: CreateServiceOrderRequest): ServiceOrderResponse

    @Operation(summary = "List all service orders with pagination")
    @ApiResponses(ApiResponse(responseCode = "200", description = "Paginated service order list"))
    fun findAll(pageable: Pageable): Page<ServiceOrderResponse>

    @Operation(summary = "Find service order by ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Service order found",
            content = [Content(schema = Schema(implementation = ServiceOrderResponse::class))]),
        ApiResponse(responseCode = "404", description = "Service order not found", content = [Content()])
    )
    fun findById(id: UUID): ServiceOrderResponse

    @Operation(summary = "Advance service order status")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Status updated",
            content = [Content(schema = Schema(implementation = ServiceOrderResponse::class))]),
        ApiResponse(responseCode = "400", description = "Invalid state transition", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Service order not found", content = [Content()])
    )
    fun advanceStatus(id: UUID, request: ServiceOrderStatusUpdateRequest): ServiceOrderResponse

    @Operation(summary = "Approve or reject service order")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Approval processed",
            content = [Content(schema = Schema(implementation = ServiceOrderResponse::class))]),
        ApiResponse(responseCode = "400", description = "Invalid approval state", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Service order not found", content = [Content()])
    )
    fun approve(id: UUID, request: ApprovalRequest): ServiceOrderResponse

    @Operation(summary = "Get execution time metrics")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Metrics retrieved",
            content = [Content(schema = Schema(implementation = ServiceOrderMetricsResponse::class))])
    )
    fun getMetrics(): ServiceOrderMetricsResponse
}
