package com.smartthings.products.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartthings.common.dto.ProductDto;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class CacheConfig {

    @Bean
    CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = cacheObjectMapper();

        RedisCacheConfiguration defaultConfiguration = redisCacheConfiguration(RedisSerializer.java());
        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                "products", redisCacheConfiguration(productsSerializer(objectMapper)),
                "productById", redisCacheConfiguration(productSerializer(objectMapper))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    static ObjectMapper cacheObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    static Jackson2JsonRedisSerializer<ProductDto> productSerializer(ObjectMapper objectMapper) {
        return new Jackson2JsonRedisSerializer<>(objectMapper, ProductDto.class);
    }

    static Jackson2JsonRedisSerializer<List<ProductDto>> productsSerializer(ObjectMapper objectMapper) {
        JavaType productsType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, ProductDto.class);
        return new Jackson2JsonRedisSerializer<>(objectMapper, productsType);
    }

    private RedisCacheConfiguration redisCacheConfiguration(RedisSerializer<?> valueSerializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
    }
}
