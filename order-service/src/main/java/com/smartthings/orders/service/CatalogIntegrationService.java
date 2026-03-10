package com.smartthings.orders.service;

import com.smartthings.common.dto.ProductDto;
import com.smartthings.common.dto.ReserveProductRequest;
import com.smartthings.common.exception.BusinessException;
import com.smartthings.orders.client.ProductClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CatalogIntegrationService {
    private static final Logger log = LoggerFactory.getLogger(CatalogIntegrationService.class);

    private final ProductClient productClient;

    public CatalogIntegrationService(ProductClient productClient) {
        this.productClient = productClient;
    }

    @CircuitBreaker(name = "product-service", fallbackMethod = "productFallback")
    public ProductDto fetchProduct(Long productId) {
        return productClient.getById(productId);
    }

    @CircuitBreaker(name = "product-service", fallbackMethod = "reserveFallback")
    public ProductDto reserveProduct(Long productId, int quantity) {
        return productClient.reserve(productId, new ReserveProductRequest(quantity));
    }

    private ProductDto productFallback(Long productId, Throwable throwable) {
        log.warn("Product service unavailable while fetching product {}", productId, throwable);
        throw new BusinessException("Product service is temporarily unavailable. Please try again later.");
    }

    private ProductDto reserveFallback(Long productId, int quantity, Throwable throwable) {
        log.warn("Product service unavailable while reserving product {}", productId, throwable);
        throw new BusinessException("Unable to reserve product right now. Please try again later.");
    }
}

