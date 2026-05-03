package com.cao.repairshop.user.service

import com.cao.repairshop.core.exception.DuplicateEntityException
import com.cao.repairshop.core.security.JwtService
import com.cao.repairshop.user.domain.UserRole
import com.cao.repairshop.user.dto.CreateUserRequest
import com.cao.repairshop.user.dto.LoginRequest
import com.cao.repairshop.user.entity.User
import com.cao.repairshop.user.repository.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @MockK
    private lateinit var jwtService: JwtService

    @InjectMockKs
    private lateinit var userService: UserService

    private fun buildUser(email: String = "user@example.com") = User(
        name = "Test User",
        function = UserRole.CUSTOMER,
        email = email,
        password = "hashed"
    )

    private fun buildCreateRequest(email: String = "user@example.com") = CreateUserRequest(
        name = "Test User",
        function = "CUSTOMER",
        email = email,
        password = "plaintext"
    )

    // -------------------------------------------------------------------------
    // createUser
    // -------------------------------------------------------------------------

    @Test
    fun `createUser success - encodes password and saves user`() {
        val user = buildUser()
        val request = buildCreateRequest()

        every { userRepository.findByEmail(request.email) } returns null
        every { passwordEncoder.encode(request.password) } returns "hashed"
        every { userRepository.save(any()) } returns user

        val result = userService.createUser(request)

        assertThat(result.email).isEqualTo("user@example.com")
        verify(exactly = 1) { passwordEncoder.encode("plaintext") }
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `createUser duplicate email - throws DuplicateEntityException`() {
        val existing = buildUser()
        val request = buildCreateRequest()

        every { userRepository.findByEmail(request.email) } returns existing

        assertThatThrownBy { userService.createUser(request) }
            .isInstanceOf(DuplicateEntityException::class.java)

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
    }

    // -------------------------------------------------------------------------
    // authenticate
    // -------------------------------------------------------------------------

    @Test
    fun `authenticate success - matches password and returns token`() {
        val user = buildUser()
        val request = LoginRequest(email = "user@example.com", password = "plaintext")

        every { userRepository.findByEmail(request.email) } returns user
        every { passwordEncoder.matches("plaintext", "hashed") } returns true
        every { jwtService.generateToken(user.id, user.function) } returns "jwt-token"

        val result = userService.authenticate(request)

        assertThat(result.token).isEqualTo("jwt-token")
        verify(exactly = 1) { jwtService.generateToken(user.id, user.function) }
    }

    @Test
    fun `authenticate wrong password - throws IllegalArgumentException`() {
        val user = buildUser()
        val request = LoginRequest(email = "user@example.com", password = "wrongpassword")

        every { userRepository.findByEmail(request.email) } returns user
        every { passwordEncoder.matches("wrongpassword", "hashed") } returns false

        assertThatThrownBy { userService.authenticate(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Invalid credentials")

        verify(exactly = 0) { jwtService.generateToken(any(), any()) }
    }

    @Test
    fun `authenticate email not found - throws IllegalArgumentException`() {
        val request = LoginRequest(email = "notfound@example.com", password = "plaintext")

        every { userRepository.findByEmail(request.email) } returns null

        assertThatThrownBy { userService.authenticate(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Invalid credentials")

        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
        verify(exactly = 0) { jwtService.generateToken(any(), any()) }
    }
}
