package com.cao.repairshop.user.service

import com.cao.repairshop.core.exception.DuplicateEntityException
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.core.exception.PasswordEncodingException
import com.cao.repairshop.core.security.JwtService
import com.cao.repairshop.user.domain.UserRole
import com.cao.repairshop.user.dto.CreateUserRequest
import com.cao.repairshop.user.dto.LoginRequest
import com.cao.repairshop.user.dto.TokenResponse
import com.cao.repairshop.user.dto.UserResponse
import com.cao.repairshop.user.entity.User
import com.cao.repairshop.user.mapper.toResponse
import com.cao.repairshop.user.repository.UserRepository
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

        val user = User(
            name = request.name,
            function = UserRole.valueOf(request.function.uppercase()),
            email = request.email,
            phone = request.phone,
            password = passwordEncoder.encode(request.password)
                ?: throw PasswordEncodingException(ErrorMessages.User.PASSWORD_ENCODING_FAILED),
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
        return user
    }
}
