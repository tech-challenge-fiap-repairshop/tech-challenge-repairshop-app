package com.cao.repairshop.user.controller

import com.cao.repairshop.user.service.UserService
import com.cao.repairshop.user.domain.UserRole
import com.cao.repairshop.user.dto.CreateUserRequest
import com.cao.repairshop.user.dto.LoginRequest
import com.cao.repairshop.user.dto.TokenResponse
import com.cao.repairshop.user.dto.UserResponse

import com.cao.repairshop.core.exception.GlobalExceptionHandler
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.json.JsonMapper
import java.util.UUID

class AuthControllerTest {

    private val userService: UserService = mockk()
    private lateinit var mockMvc: MockMvc
    private lateinit var mapper: JsonMapper

    @BeforeEach
    fun setUp() {
        mapper = JsonMapper.builder().build()

        mockMvc = MockMvcBuilders
            .standaloneSetup(AuthController(userService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setMessageConverters(JacksonJsonHttpMessageConverter(mapper))
            .build()
    }

    @Test
    fun `POST auth login should return 200 with valid credentials`() {
        val request = LoginRequest(email = "user@test.com", password = "secret123")
        every { userService.authenticate(any()) } returns TokenResponse(token = "jwt-token-here")

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("jwt-token-here"))
    }

    @Test
    fun `POST auth login should return 400 with blank email`() {
        val request = mapOf("email" to "", "password" to "secret123")

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST auth register should return 201`() {
        val request = CreateUserRequest(
            name = "John Doe",
            function = "ATTENDANT",
            email = "john@test.com",
            phone = "+55 11 98888-7777",
            password = "pass1234"
        )
        val userResponse = UserResponse(
            id = UUID.randomUUID(),
            name = "John Doe",
            function = UserRole.ATTENDANT,
            email = "john@test.com",
            phone = "+55 11 98888-7777"
        )
        every { userService.createUser(any()) } returns userResponse

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.email").value("john@test.com"))
            .andExpect(jsonPath("$.function").value("ATTENDANT"))
    }
}
