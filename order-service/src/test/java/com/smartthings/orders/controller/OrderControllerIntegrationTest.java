package com.smartthings.orders.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartthings.common.dto.CreateOrderRequest;
import com.smartthings.common.dto.OrderDto;
import com.smartthings.common.dto.OrderItemRequest;
import com.smartthings.common.dto.OrderItemSnapshotDto;
import com.smartthings.common.enums.OrderStatus;
import com.smartthings.orders.service.CatalogIntegrationService;
import com.smartthings.orders.service.NotificationIntegrationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:order-service-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogIntegrationService catalogIntegrationService;
    @MockBean
    private NotificationIntegrationService notificationIntegrationService;

    @Test
    void createOrderPersistsAndReturnsJson() throws Exception {
        Mockito.when(catalogIntegrationService.fetchProduct(1L)).thenReturn(
                new com.smartthings.common.dto.ProductDto(1L, "Smart Lamp", "desc", "Lighting", "Brand",
                        new BigDecimal("2490"), "RUB", 10, null, true, Instant.now(), Instant.now())
        );
        Mockito.when(catalogIntegrationService.reserveProduct(1L, 1)).thenReturn(
                new com.smartthings.common.dto.ProductDto(1L, "Smart Lamp", "desc", "Lighting", "Brand",
                        new BigDecimal("2490"), "RUB", 9, null, true, Instant.now(), Instant.now())
        );

        CreateOrderRequest request = new CreateOrderRequest(
                "Buyer",
                "buyer@smartthings.local",
                "Moscow",
                "Leave at concierge",
                List.of(new OrderItemRequest(1L, 1))
        );

        mockMvc.perform(post("/api/orders")
                        .header("X-Auth-User-Id", "3")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.items[0].productName").value("Smart Lamp"));
    }

    @Test
    void updateStatusAndListOrdersWorkAgainstDatabase() throws Exception {
        Mockito.when(catalogIntegrationService.fetchProduct(1L)).thenReturn(
                new com.smartthings.common.dto.ProductDto(1L, "Smart Lamp", "desc", "Lighting", "Brand",
                        new BigDecimal("2490"), "RUB", 10, null, true, Instant.now(), Instant.now())
        );
        Mockito.when(catalogIntegrationService.reserveProduct(1L, 1)).thenReturn(
                new com.smartthings.common.dto.ProductDto(1L, "Smart Lamp", "desc", "Lighting", "Brand",
                        new BigDecimal("2490"), "RUB", 9, null, true, Instant.now(), Instant.now())
        );

        CreateOrderRequest request = new CreateOrderRequest(
                "Buyer",
                "buyer2@smartthings.local",
                "Moscow",
                null,
                List.of(new OrderItemRequest(1L, 1))
        );

        String response = mockMvc.perform(post("/api/orders")
                        .header("X-Auth-User-Id", "4")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDto created = objectMapper.readValue(response, OrderDto.class);

        mockMvc.perform(patch("/api/orders/{id}/status", created.id())
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/api/orders").header("X-Auth-User-Id", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PAID"));
    }
}
