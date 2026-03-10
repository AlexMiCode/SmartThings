package com.smartthings.products.config;

import com.smartthings.common.dto.ProductDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void productSerializerRoundTripsProductDto() {
        ProductDto product = new ProductDto(
                1L,
                "Smart Hub",
                "Central control hub",
                "Hubs",
                "Aqara",
                new BigDecimal("7990"),
                "RUB",
                7,
                "https://example.com/hub.png",
                true,
                Instant.parse("2026-03-10T19:00:00Z"),
                Instant.parse("2026-03-10T19:05:00Z")
        );

        byte[] serialized = CacheConfig.productSerializer(CacheConfig.cacheObjectMapper()).serialize(product);
        ProductDto restored = CacheConfig.productSerializer(CacheConfig.cacheObjectMapper()).deserialize(serialized);

        assertThat(restored).isEqualTo(product);
    }

    @Test
    void productsSerializerRoundTripsProductList() {
        List<ProductDto> products = List.of(
                new ProductDto(
                        1L,
                        "Smart Hub",
                        "Central control hub",
                        "Hubs",
                        "Aqara",
                        new BigDecimal("7990"),
                        "RUB",
                        7,
                        "https://example.com/hub.png",
                        true,
                        Instant.parse("2026-03-10T19:00:00Z"),
                        Instant.parse("2026-03-10T19:05:00Z")
                ),
                new ProductDto(
                        2L,
                        "Smart Plug",
                        "Wi-Fi plug",
                        "Sockets",
                        "TP-Link",
                        new BigDecimal("2490"),
                        "RUB",
                        20,
                        "https://example.com/plug.png",
                        false,
                        Instant.parse("2026-03-10T19:01:00Z"),
                        Instant.parse("2026-03-10T19:06:00Z")
                )
        );

        byte[] serialized = CacheConfig.productsSerializer(CacheConfig.cacheObjectMapper()).serialize(products);
        List<ProductDto> restored = CacheConfig.productsSerializer(CacheConfig.cacheObjectMapper()).deserialize(serialized);

        assertThat(restored).isEqualTo(products);
    }
}
