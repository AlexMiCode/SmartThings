package com.smartthings.products.service;

import com.smartthings.common.dto.CreateProductRequest;
import com.smartthings.common.dto.ProductDto;
import com.smartthings.common.dto.UpdateProductRequest;
import com.smartthings.common.exception.BusinessException;
import com.smartthings.common.exception.NotFoundException;
import com.smartthings.products.entity.Product;
import com.smartthings.products.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class ProductCatalogService {
    private static final Logger log = LoggerFactory.getLogger(ProductCatalogService.class);

    private final ProductRepository productRepository;

    public ProductCatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable("products")
    public List<ProductDto> findAll(BigDecimal minPrice, BigDecimal maxPrice, String category, String search) {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .filter(product -> minPrice == null || product.getPrice().compareTo(minPrice) >= 0)
                .filter(product -> maxPrice == null || product.getPrice().compareTo(maxPrice) <= 0)
                .filter(product -> category == null || category.isBlank() || product.getCategory().equalsIgnoreCase(category))
                .filter(product -> search == null || search.isBlank()
                        || product.getName().toLowerCase().contains(search.toLowerCase())
                        || product.getDescription().toLowerCase().contains(search.toLowerCase()))
                .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                .map(this::toDto)
                .toList();
    }

    @Cacheable(value = "productById", key = "#id")
    public ProductDto findById(Long id) {
        return toDto(getById(id));
    }

    @CacheEvict(value = {"products", "productById"}, allEntries = true)
    public ProductDto create(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setBrand(request.brand());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setImageUrl(request.imageUrl());
        product.setFeatured(request.featured());
        Product saved = productRepository.save(product);
        log.info("Created product with id={}", saved.getId());
        return toDto(saved);
    }

    @CacheEvict(value = {"products", "productById"}, allEntries = true)
    public ProductDto update(Long id, UpdateProductRequest request) {
        Product product = getById(id);

        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.category() != null) {
            product.setCategory(request.category());
        }
        if (request.brand() != null) {
            product.setBrand(request.brand());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.stockQuantity() != null) {
            product.setStockQuantity(request.stockQuantity());
        }
        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }
        if (request.featured() != null) {
            product.setFeatured(request.featured());
        }

        Product saved = productRepository.save(product);
        log.info("Updated product with id={}", saved.getId());
        return toDto(saved);
    }

    @CacheEvict(value = {"products", "productById"}, allEntries = true)
    public ProductDto reserve(Long id, int quantity) {
        Product product = getById(id);
        if (product.getStockQuantity() < quantity) {
            throw new BusinessException("Not enough stock for product " + product.getName());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        Product saved = productRepository.save(product);
        log.info("Reserved {} item(s) for product id={}", quantity, id);
        return toDto(saved);
    }

    @CacheEvict(value = {"products", "productById"}, allEntries = true)
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product with id " + id + " not found");
        }
        productRepository.deleteById(id);
        log.info("Deleted product with id={}", id);
    }

    private Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product with id " + id + " not found"));
    }

    private ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getBrand(),
                product.getPrice(),
                product.getCurrency(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.isFeatured(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}

