package com.shopwise.gateway.filter;

import com.shopwise.gateway.config.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtService jwtService;
    private final ReactiveStringRedisTemplate redisTemplate;

    public AuthenticationFilter(JwtService jwtService,
                                ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange, "Token bulunamadı");
            }

            String token = authHeader.substring(7);

            if (!jwtService.isTokenValid(token)) {
                return unauthorized(exchange, "Geçersiz token");
            }

            Long userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);
            String redisKey = "whitelist:" + userId;

            return redisTemplate.opsForValue().get(redisKey)
                    .defaultIfEmpty("")
                    .flatMap(storedToken -> {
                        if (storedToken.isEmpty()) {
                            return unauthorized(exchange, "Geçersiz token");
                        }

                        if (!storedToken.equals(token)) {
                            return unauthorized(exchange, "Token geçersiz veya iptal edilmiş");
                        }

                        ServerWebExchange modifiedExchange = exchange.mutate()
                                .request(r -> r
                                        .header("X-User-Id", String.valueOf(userId))
                                        .header("X-User-Role", role)
                                        .header("Authorization", "Bearer " + token))
                                .build();

                        return chain.filter(modifiedExchange);
                    });
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        log.warn("Yetkisiz erişim: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        byte[] bytes = ("{\"errorCode\":\"UNAUTHORIZED\",\"message\":\"" + message + "\"}").getBytes();
        org.springframework.core.io.buffer.DataBuffer buffer =
                exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    public static class Config {}
}