package com.coders.commagateway.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SetPathGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AddAuthInfoGatewayFilterFactory extends AbstractGatewayFilterFactory<AddAuthInfoGatewayFilterFactory.Config> {

    public AddAuthInfoGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) ->
                ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .filter(auth -> auth.isAuthenticated() && auth.getPrincipal() != null)
                        .map(authentication -> {
                            ServerHttpRequest request = exchange.getRequest().mutate()
                                    .header("X-User-Email", authentication.getName())
                                    .build();
                            return exchange.mutate().request(request).build();
                        })
                        .defaultIfEmpty(exchange)
                        .flatMap(chain::filter);
    }

    @Getter
    @Setter
    public static class Config {

    }
}
