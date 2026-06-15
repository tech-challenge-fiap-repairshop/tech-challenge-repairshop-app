package com.cao.repairshop.user.application.usecases.impl

import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.core.security.JwtService
import com.cao.repairshop.user.application.gateways.UserGateway
import com.cao.repairshop.user.domain.entities.User
import com.cao.repairshop.user.domain.entities.UserRole
import com.cao.repairshop.user.infra.controller.dtos.LoginRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class AuthenticateUserImplTest {

    private lateinit var userGateway: UserGateway
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtService: JwtService
    private lateinit var authenticateUserImpl: AuthenticateUserImpl

    @BeforeEach
    fun setup() {
        userGateway = mockk()
        passwordEncoder = mockk()
        jwtService = mockk()
        authenticateUserImpl = AuthenticateUserImpl(userGateway, passwordEncoder, jwtService)
    }

    @Test
    fun `should authenticate user successfully`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "password"
        )

        val user = User(
            name = "Test User",
            email = "test@example.com",
            function = UserRole.ATTENDANT,
            password = "encodedPassword",
            phone = "11999999999"
        )

        every { userGateway.findByEmail(request.email) } returns user
        every { passwordEncoder.matches(request.password, user.password) } returns true
        every { jwtService.generateToken(user.id, user.function) } returns "generatedToken"

        val response = authenticateUserImpl.execute(request)

        assertEquals("generatedToken", response.token)
        
        verify { userGateway.findByEmail(request.email) }
        verify { passwordEncoder.matches(request.password, user.password) }
        verify { jwtService.generateToken(user.id, user.function) }
    }

    @Test
    fun `should throw error when user not found`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "password"
        )

        every { userGateway.findByEmail(request.email) } returns null

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authenticateUserImpl.execute(request)
        }

        assertEquals(ErrorMessages.User.INVALID_CREDENTIALS, exception.message)
        
        verify { userGateway.findByEmail(request.email) }
    }
    
    @Test
    fun `should throw error when password does not match`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        val user = User(
            name = "Test User",
            email = "test@example.com",
            function = UserRole.ATTENDANT,
            password = "encodedPassword",
            phone = "11999999999"
        )

        every { userGateway.findByEmail(request.email) } returns user
        every { passwordEncoder.matches(request.password, user.password) } returns false

        val exception = assertThrows(IllegalArgumentException::class.java) {
            authenticateUserImpl.execute(request)
        }

        assertEquals(ErrorMessages.User.INVALID_CREDENTIALS, exception.message)
        
        verify { userGateway.findByEmail(request.email) }
        verify { passwordEncoder.matches(request.password, user.password) }
    }
}
