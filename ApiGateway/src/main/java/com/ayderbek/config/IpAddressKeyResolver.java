package com.ayderbek.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component("ipAddressKeyResolver")
public class IpAddressKeyResolver implements KeyResolver {
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        // Extract the access token from the request headers
        String accessToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        // Use the access token as part of the key for rate-limiting
        String ipAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        String key = ipAddress + ":" + (accessToken != null ? accessToken : "anonymous");
        return Mono.just(key);
    }
}

