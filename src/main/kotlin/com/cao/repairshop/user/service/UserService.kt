package com.cao.repairshop.user.service

import com.cao.repairshop.user.entity.User
import com.cao.repairshop.user.domain.UserRole
import com.cao.repairshop.user.dto.CreateUserRequest
import com.cao.repairshop.user.dto.LoginRequest
import com.cao.repairshop.user.dto.TokenResponse
import com.cao.repairshop.user.dto.UserResponse
import com.cao.repairshop.user.mapper.toResponse
import com.cao.repairshop.user.repository.UserRepository

import com.cao.repairshop.core.exception.BusinessRuleViolationException
import com.cao.repairshop.core.exception.DuplicateEntityException
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.core.security.JwtService
import com.cao.repairshop.core.exception.PasswordEncodingException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        userRepository.findByEmail(request.email)?.let {
            throw DuplicateEntityException(ErrorMessages.User.DUPLICATE_EMAIL)
        }

        val role = UserRole.valueOf(request.function.uppercase())
        if (role != UserRole.CUSTOMER)
            throw BusinessRuleViolationException("Self-registration is only allowed for CUSTOMER role. Contact an administrator for other roles.")

        val user = User(
            name = request.name,
            function = role,
            email = request.email,
            phone = request.phone,
            password = passwordEncoder.encode(request.password)
                ?: throw PasswordEncodingException(ErrorMessages.User.PASSWORD_ENCODING_FAILED)
        )

        return userRepository.save(user).toResponse()
    }

    @Transactional(readOnly = true)
    fun authenticate(request: LoginRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email)
            ?.takeIf { passwordEncoder.matches(request.password, it.password) }
            ?: throw IllegalArgumentException(ErrorMessages.User.INVALID_CREDENTIALS)

        val token = jwtService.generateToken(user.id, user.function)
        return TokenResponse(token = token)
    }

    fun verifyRegisteredCustomer(email: String): User {
        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException(ErrorMessages.Customer.USER_NOT_FOUND_FOR_EMAIL)
        if (user.function != UserRole.CUSTOMER)
            throw BusinessRuleViolationException(ErrorMessages.Customer.USER_NOT_CUSTOMER_ROLE)
        return user
    }
}
