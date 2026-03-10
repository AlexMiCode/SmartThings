package com.smartthings.orders.client;

import com.smartthings.common.dto.ProductDto;
import com.smartthings.common.dto.ReserveProductRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    ProductDto getById(@PathVariable("id") Long id);

    @PostMapping("/api/products/{id}/reserve")
    ProductDto reserve(@PathVariable("id") Long id, @RequestBody ReserveProductRequest request);
}

