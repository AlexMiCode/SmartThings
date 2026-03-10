package com.smartthings.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartthings.common.dto.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "security.jwt.secret=U21hcnRUaGluZ3NTdG9yZVN1cGVyU2VjcmV0S2V5Rm9ySldUMTIzNDU2Nzg5MDEyMzQ1",
        "security.jwt.expiration-seconds=3600",
        "spring.datasource.url=jdbc:h2:mem:user-service-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUserPersistsAndReturnsPayload() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "Integration User",
                "integration-user@smartthings.local",
                "secret123",
                "customer"
        );

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("integration-user@smartthings.local"));
    }

    @Test
    void getUsersReturnsSeededAdmin() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").exists());
    }
}
