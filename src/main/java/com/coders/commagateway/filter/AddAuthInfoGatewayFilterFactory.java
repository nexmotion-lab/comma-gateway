package com.coders.commagateway.filter;

import com.coders.commagateway.filter.service.FindIdService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class AddAuthInfoGatewayFilterFactory extends AbstractGatewayFilterFactory<AddAuthInfoGatewayFilterFactory.Config> {

    private final FindIdService findIdService;

    @Autowired
    public AddAuthInfoGatewayFilterFactory(FindIdService findIdService) {
        super(Config.class);
        this.findIdService = findIdService;
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) ->
                ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .filter(auth -> auth.isAuthenticated() && auth.getPrincipal() != null)
                        .flatMap(authentication ->
                                findIdService.findAccountIdByEmail(authentication.getName())
                                        .map(id -> {
                                            ServerHttpRequest request = exchange.getRequest().mutate()
                                                    .header("X-User-Id", id)
                                                    .build();
                                            log.info("Added X-User-Id header: {}", id);

                                            return exchange.mutate().request(request).build();
                                        })
                                        .onErrorResume(e -> {
                                            log.error("Error fetching account ID for email {}: {}", authentication.getName(), e.getMessage());
                                            return Mono.just(exchange);
                                        })
                        )
                        .defaultIfEmpty(exchange)
                        .flatMap(chain::filter);
    }


    @Getter
    @Setter
    public static class Config {

    }
}
