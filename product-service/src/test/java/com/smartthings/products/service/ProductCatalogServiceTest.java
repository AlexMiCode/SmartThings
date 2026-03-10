package com.smartthings.products.service;

import com.smartthings.common.dto.CreateProductRequest;
import com.smartthings.common.dto.UpdateProductRequest;
import com.smartthings.common.exception.BusinessException;
import com.smartthings.common.exception.NotFoundException;
import com.smartthings.products.entity.Product;
import com.smartthings.products.repository.ProductRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCatalogServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductCatalogService productCatalogService;

    private Product lamp;
    private Product sensor;

    @BeforeEach
    void setUp() {
        lamp = product(1L, "Smart Lamp", "Lighting", new BigDecimal("2490"), 10);
        sensor = product(2L, "Leak Sensor", "Safety", new BigDecimal("1990"), 2);
    }

    @Test
    void findAllFiltersByPriceCategoryAndSearch() {
        when(productRepository.findAll()).thenReturn(List.of(lamp, sensor));

        var result = productCatalogService.findAll(new BigDecimal("2000"), new BigDecimal("3000"), "Lighting", "lamp");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Smart Lamp");
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productCatalogService.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createStoresNewProduct() {
        CreateProductRequest request = new CreateProductRequest(
                "New Hub", "Hub", "Hub", "Aqara", new BigDecimal("9990"), 5, null, true
        );
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            setMeta(product, 3L);
            return product;
        });

        var response = productCatalogService.create(request);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.price()).isEqualTo(new BigDecimal("9990"));
    }

    @Test
    void updateChangesMutableFields() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(lamp));
        when(productRepository.save(lamp)).thenReturn(lamp);

        var response = productCatalogService.update(1L, new UpdateProductRequest(
                "Smart Lamp Pro", null, null, null, new BigDecimal("2990"), 7, null, false
        ));

        assertThat(response.name()).isEqualTo("Smart Lamp Pro");
        assertThat(response.price()).isEqualTo(new BigDecimal("2990"));
        assertThat(response.stockQuantity()).isEqualTo(7);
    }

    @Test
    void reserveRejectsInsufficientStock() {
        when(productRepository.findById(2L)).thenReturn(Optional.of(sensor));

        assertThatThrownBy(() -> productCatalogService.reserve(2L, 5))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Not enough stock");
    }

    private Product product(Long id, String name, String category, BigDecimal price, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(name + " description");
        product.setCategory(category);
        product.setBrand("Brand");
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setFeatured(true);
        setMeta(product, id);
        return product;
    }

    private void setMeta(Product product, Long id) {
        try {
            var idField = Product.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(product, id);
            var createdAtField = Product.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(product, Instant.now());
            var updatedAtField = Product.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(product, Instant.now());
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}

