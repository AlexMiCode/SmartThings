package com.smartthings.orders.controller;

import com.smartthings.common.dto.CreateOrderRequest;
import com.smartthings.common.dto.OrderDto;
import com.smartthings.common.enums.OrderStatus;
import com.smartthings.orders.service.OrderApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "X-Auth-User-Id", required = false) String userIdHeader
    ) {
        return orderApplicationService.create(request, userIdHeader);
    }

    @GetMapping
    public List<OrderDto> findForCurrentUser(
            @RequestHeader(value = "X-Auth-User-Id", required = false) String userIdHeader,
            @RequestParam(required = false) Long userId
    ) {
        return orderApplicationService.findByUserId(userIdHeader, userId);
    }

    @PatchMapping("/{id}/status")
    public OrderDto updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return orderApplicationService.updateStatus(id, status);
    }
}

