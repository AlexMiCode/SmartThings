package com.smartthings.products.controller;

import com.smartthings.common.dto.CreateProductRequest;
import com.smartthings.common.dto.ProductDto;
import com.smartthings.common.dto.ReserveProductRequest;
import com.smartthings.common.dto.UpdateProductRequest;
import com.smartthings.products.service.ProductCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductCatalogService productCatalogService;

    public ProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @GetMapping
    public List<ProductDto> findAll(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        return productCatalogService.findAll(minPrice, maxPrice, category, search);
    }

    @GetMapping("/{id}")
    public ProductDto findById(@PathVariable Long id) {
        return productCatalogService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto create(@Valid @RequestBody CreateProductRequest request) {
        return productCatalogService.create(request);
    }

    @PutMapping("/{id}")
    public ProductDto update(@PathVariable Long id, @RequestBody UpdateProductRequest request) {
        return productCatalogService.update(id, request);
    }

    @PostMapping("/{id}/reserve")
    public ProductDto reserve(@PathVariable Long id, @Valid @RequestBody ReserveProductRequest request) {
        return productCatalogService.reserve(id, request.quantity());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productCatalogService.delete(id);
    }
}

