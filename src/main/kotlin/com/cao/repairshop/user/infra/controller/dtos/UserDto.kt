package com.cao.repairshop.user.infra.controller.dtos

import com.cao.repairshop.core.validator.annotation.Phone
import com.cao.repairshop.core.validator.annotation.SafeString
import com.cao.repairshop.core.validator.annotation.ValidRole
import com.cao.repairshop.core.validator.annotation.VerifyEmail
import com.cao.repairshop.user.domain.entities.UserRole
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class CreateUserRequest(
    @Schema(description = "User full name", example = "Carlos Mechanic")
    @field:NotBlank @field:SafeString val name: String,

    @Schema(description = "User role", example = "ATTENDANT", allowableValues = ["CUSTOMER", "ATTENDANT"])
    @field:NotBlank @field:ValidRole val function: String,

    @Schema(description = "User email (used for login)", example = "carlos@repairshop.com")
    @field:NotBlank @field:VerifyEmail val email: String,

    @Schema(description = "User phone number", example = "+55 11 99999-9999")
    @field:NotBlank @field:Phone val phone: String? = null,

    @Schema(description = "Account password", example = "securePass123")
    @field:NotBlank val password: String
)

data class LoginRequest(
    @Schema(description = "User email", example = "carlos@repairshop.com")
    @field:NotBlank @field:VerifyEmail val email: String,

    @Schema(description = "Account password", example = "securePass123")
    @field:NotBlank val password: String
)

data class TokenResponse(
    @Schema(description = "JWT authentication token")
    val token: String
)

data class UserResponse(
    @Schema(description = "User unique identifier")
    val id: UUID,
    @Schema(description = "User full name", example = "Carlos Mechanic")
    val name: String,
    @Schema(description = "User role", example = "ATTENDANT")
    val function: UserRole,
    @Schema(description = "User email", example = "carlos@repairshop.com")
    val email: String,
    @Schema(description = "User phone number", example = "+55 11 99999-9999")
    val phone: String?
)
