package com.example.gatewayservice.config.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;


@Component
@Slf4j
public class CustomAuthFilter extends AbstractGatewayFilterFactory<CustomAuthFilter.Config> {
    public CustomAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();  // PreFilter

            // Request Header token 이 없는 경우
            if(!request.getHeaders().containsKey("token"))
                return handleUnAuthorized(exchange);  // 401

            List<String> token = request.getHeaders().get("token");
            String tokenString = Objects.requireNonNull(token).get(0);

            // token 검증
            if(!"1234".equals(tokenString)) {
                return handleUnAuthorized(exchange);
            }

            log.info("Pre CustomAuhFilter | request id : " + request.getId());
            // token 확인
            return chain.filter(exchange);
        };
    }

    private Mono<Void> handleUnAuthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();  // PostFilter

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        log.info("Post handleUnAuthorized | response code -> {}", response.getStatusCode());

        return response.setComplete();
    }

    public static class Config {
        // put the configuration properties
    }
}
