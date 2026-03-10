package com.smartthings.products.support;

import com.smartthings.products.entity.Product;
import com.smartthings.products.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductDataInitializer implements CommandLineRunner {
    private final ProductRepository productRepository;

    public ProductDataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        productRepository.saveAll(List.of(
                create("Умная лампа Yeelight", "Регулировка яркости и температуры света, поддержка сценариев умного дома.", "Lighting", "Yeelight", new BigDecimal("2490"), 25, "https://images.unsplash.com/photo-1513694203232-719a280e022f"),
                create("Датчик протечки Aqara", "Компактный сенсор для контроля утечек воды и мгновенных уведомлений.", "Safety", "Aqara", new BigDecimal("1990"), 18, "https://images.unsplash.com/photo-1558002038-1055907df827"),
                create("Умная розетка TP-Link Tapo", "Удаленное включение техники, таймеры и статистика потребления.", "Power", "TP-Link", new BigDecimal("1690"), 30, "https://images.unsplash.com/photo-1585771724684-38269d6639fd"),
                create("Умный термостат Moes", "Контроль температуры в квартире и недельные сценарии отопления.", "Climate", "Moes", new BigDecimal("7490"), 12, "https://images.unsplash.com/photo-1527689368864-3a821dbccc34")
        ));
    }

    private Product create(String name, String description, String category, String brand, BigDecimal price, int stock, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setBrand(brand);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setImageUrl(imageUrl);
        product.setFeatured(true);
        return product;
    }
}

