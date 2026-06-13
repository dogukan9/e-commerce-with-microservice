package com.shopwise.gateway.config;

import com.shopwise.gateway.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.product-service.url}")
    private String productServiceUrl;

    @Value("${services.order-service.url}")
    private String orderServiceUrl;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder, AuthenticationFilter authFilter) {
        return builder.routes()

                // User Service — public
                .route("user-service-public", r -> r
                        .path("/api/v1/users/login", "/api/v1/users")
                        .and().method("POST")
                        .uri(userServiceUrl))

                .route("user-service-protected", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri(userServiceUrl))

                .route("product-service", r -> r
                        .path("/api/v1/products/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri(productServiceUrl))

                .route("order-service", r -> r
                        .path("/api/v1/orders/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri(orderServiceUrl))

                .build();
    }

}