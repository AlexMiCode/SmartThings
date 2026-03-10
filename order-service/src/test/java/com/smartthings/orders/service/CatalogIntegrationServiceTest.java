package com.smartthings.orders.service;

import com.smartthings.common.dto.ProductDto;
import com.smartthings.common.exception.BusinessException;
import com.smartthings.orders.client.ProductClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogIntegrationServiceTest {

    @Test
    void fetchProductFallbackReturnsBusinessExceptionWhenCatalogUnavailable() {
        ProductClient productClient = mock(ProductClient.class);
        CatalogIntegrationService service = new CatalogIntegrationService(productClient);
        when(productClient.getById(1L)).thenThrow(new RuntimeException("down"));

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "productFallback", 1L, new RuntimeException("down")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("temporarily unavailable");
    }

    @Test
    void reserveProductFallbackReturnsBusinessExceptionWhenCatalogUnavailable() {
        ProductClient productClient = mock(ProductClient.class);
        CatalogIntegrationService service = new CatalogIntegrationService(productClient);

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "reserveFallback", 1L, 2, new RuntimeException("down")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unable to reserve product");
    }

    @Test
    void fetchProductDelegatesToClient() {
        ProductClient productClient = mock(ProductClient.class);
        CatalogIntegrationService service = new CatalogIntegrationService(productClient);
        ProductDto dto = new ProductDto(1L, "Smart Lamp", "desc", "Lighting", "Brand", BigDecimal.ONE,
                "RUB", 10, null, false, Instant.now(), Instant.now());
        when(productClient.getById(1L)).thenReturn(dto);

        var result = service.fetchProduct(1L);

        org.assertj.core.api.Assertions.assertThat(result.id()).isEqualTo(1L);
    }
}

