package com.smartthings.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartthings.common.dto.AuthRequest;
import com.smartthings.common.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "security.jwt.secret=U21hcnRUaGluZ3NTdG9yZVN1cGVyU2VjcmV0S2V5Rm9ySldUMTIzNDU2Nzg5MDEyMzQ1",
        "security.jwt.expiration-seconds=3600",
        "spring.datasource.url=jdbc:h2:mem:user-auth-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerReturnsTokenAndUserJson() throws Exception {
        RegisterRequest request = new RegisterRequest("Api User", "api-user@smartthings.local", "secret123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("api-user@smartthings.local"));
    }

    @Test
    void loginReturnsTokenForExistingUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("Login User", "login-user@smartthings.local", "secret123"))));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new AuthRequest("login-user@smartthings.local", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.fullName").value("Login User"));
    }
}
