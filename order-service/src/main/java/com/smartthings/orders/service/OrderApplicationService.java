package com.smartthings.orders.service;

import com.smartthings.common.dto.CreateOrderRequest;
import com.smartthings.common.dto.OrderDto;
import com.smartthings.common.dto.OrderItemRequest;
import com.smartthings.common.dto.OrderItemSnapshotDto;
import com.smartthings.common.dto.ProductDto;
import com.smartthings.common.enums.OrderStatus;
import com.smartthings.common.exception.NotFoundException;
import com.smartthings.orders.entity.Order;
import com.smartthings.orders.entity.OrderItem;
import com.smartthings.orders.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderApplicationService {
    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepository orderRepository;
    private final CatalogIntegrationService catalogIntegrationService;
    private final NotificationIntegrationService notificationIntegrationService;

    public OrderApplicationService(
            OrderRepository orderRepository,
            CatalogIntegrationService catalogIntegrationService,
            NotificationIntegrationService notificationIntegrationService
    ) {
        this.orderRepository = orderRepository;
        this.catalogIntegrationService = catalogIntegrationService;
        this.notificationIntegrationService = notificationIntegrationService;
    }

    @Transactional
    public OrderDto create(CreateOrderRequest request, String userIdHeader) {
        Long userId = userIdHeader == null || userIdHeader.isBlank() ? 1L : Long.parseLong(userIdHeader);

        Order order = new Order();
        order.setUserId(userId);
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setDeliveryAddress(request.deliveryAddress());
        order.setNotes(request.notes());
        order.setStatus(OrderStatus.NEW);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            ProductDto product = catalogIntegrationService.fetchProduct(itemRequest.productId());
            catalogIntegrationService.reserveProduct(product.id(), itemRequest.quantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.id());
            item.setProductName(product.name());
            item.setPrice(product.price());
            item.setQuantity(itemRequest.quantity());
            order.getItems().add(item);

            total = total.add(product.price().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        notificationIntegrationService.sendOrderCreated(saved.getId(), saved.getUserId(), saved.getCustomerName());
        log.info("Created order id={} for userId={}", saved.getId(), saved.getUserId());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findByUserId(String userIdHeader, Long userIdParam) {
        Long userId = userIdParam != null ? userIdParam : (userIdHeader == null || userIdHeader.isBlank() ? 1L : Long.parseLong(userIdHeader));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public OrderDto updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id " + id + " not found"));
        order.setStatus(status);
        log.info("Updated order {} to status {}", id, status);
        return toDto(orderRepository.save(order));
    }

    private OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getUserId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getDeliveryAddress(),
                order.getNotes(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(item -> new OrderItemSnapshotDto(item.getProductId(), item.getProductName(), item.getQuantity(), item.getPrice()))
                        .toList()
        );
    }
}

