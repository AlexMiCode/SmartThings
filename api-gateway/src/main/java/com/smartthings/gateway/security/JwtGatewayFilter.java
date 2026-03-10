package com.smartthings.gateway.security;

import com.smartthings.common.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth/",
            "/api/products",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator",
            "/eureka"
    );

    private final JwtService jwtService;

    public JwtGatewayFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        if (isPublic(path, method)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Claims claims;
        try {
            claims = jwtService.parseToken(authHeader.substring(7));
        } catch (Exception ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String role = claims.get("role", String.class);
        if (requiresAdmin(path, method) && !"ADMIN".equalsIgnoreCase(role)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header("X-Auth-User-Id", claims.get("userId", String.class))
                .header("X-Auth-User-Email", claims.get("email", String.class))
                .header("X-Auth-User-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isPublic(String path, HttpMethod method) {
        if (method == HttpMethod.GET && path.startsWith("/api/products")) {
            return true;
        }

        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private boolean requiresAdmin(String path, HttpMethod method) {
        if (method == null) {
            return false;
        }
        return (path.startsWith("/api/products") && method != HttpMethod.GET)
                || path.startsWith("/api/notifications")
                || (path.startsWith("/api/users") && method != HttpMethod.GET);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
