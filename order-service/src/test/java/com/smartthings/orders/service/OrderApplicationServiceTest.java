package com.smartthings.orders.service;

import com.smartthings.common.dto.CreateOrderRequest;
import com.smartthings.common.dto.OrderItemRequest;
import com.smartthings.common.dto.ProductDto;
import com.smartthings.common.enums.OrderStatus;
import com.smartthings.common.exception.NotFoundException;
import com.smartthings.orders.entity.Order;
import com.smartthings.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CatalogIntegrationService catalogIntegrationService;
    @Mock
    private NotificationIntegrationService notificationIntegrationService;

    @InjectMocks
    private OrderApplicationService orderApplicationService;

    private ProductDto lamp;

    @BeforeEach
    void setUp() {
        lamp = new ProductDto(1L, "Smart Lamp", "desc", "Lighting", "Brand", new BigDecimal("2490"),
                "RUB", 10, null, true, Instant.now(), Instant.now());
    }

    @Test
    void createBuildsOrderReservesProductAndSendsNotification() {
        CreateOrderRequest request = new CreateOrderRequest(
                "Demo User",
                "demo@smartthings.local",
                "Moscow",
                "Call me",
                List.of(new OrderItemRequest(1L, 2))
        );
        when(catalogIntegrationService.fetchProduct(1L)).thenReturn(lamp);
        when(catalogIntegrationService.reserveProduct(1L, 2)).thenReturn(lamp);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            setMeta(order, 100L);
            return order;
        });

        var response = orderApplicationService.create(request, "5");

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.totalAmount()).isEqualByComparingTo("4980");
        assertThat(response.status()).isEqualTo(OrderStatus.NEW);
        verify(notificationIntegrationService).sendOrderCreated(100L, 5L, "Demo User");
    }

    @Test
    void findByUserIdUsesExplicitParameterWhenProvided() {
        Order order = new Order();
        order.setUserId(8L);
        order.setCustomerName("Demo");
        order.setCustomerEmail("demo@smartthings.local");
        order.setDeliveryAddress("Address");
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(new BigDecimal("2490"));
        setMeta(order, 1L);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(8L)).thenReturn(List.of(order));

        var result = orderApplicationService.findByUserId("5", 8L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().userId()).isEqualTo(8L);
    }

    @Test
    void updateStatusChangesPersistedStatus() {
        Order order = new Order();
        order.setStatus(OrderStatus.NEW);
        order.setUserId(1L);
        order.setCustomerName("Demo");
        order.setCustomerEmail("demo@smartthings.local");
        order.setDeliveryAddress("Address");
        order.setTotalAmount(BigDecimal.ONE);
        setMeta(order, 7L);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        var result = orderApplicationService.updateStatus(7L, OrderStatus.PAID);

        assertThat(result.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void updateStatusThrowsWhenOrderMissing() {
        when(orderRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderApplicationService.updateStatus(9L, OrderStatus.CANCELLED))
                .isInstanceOf(NotFoundException.class);
    }

    private void setMeta(Order order, Long id) {
        try {
            var idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, id);
            var createdAtField = Order.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(order, Instant.now());
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}

