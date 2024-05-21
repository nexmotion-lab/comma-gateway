package com.coders.commagateway.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
@Slf4j
public class TokenReactiveAuthenticationManagerResolver implements ReactiveAuthenticationManagerResolver<ServerWebExchange> {


    private final TokenAuthenticationConverter authenticationConverter;
    private final AccessTokenAuthenticationManager authenticationManager;
    private final RefreshTokenAuthenticationManager refreshTokenAuthenticationManager;

    @Override
    public Mono<ReactiveAuthenticationManager> resolve(ServerWebExchange exchange) {
        return authenticationConverter.convert(exchange)
                .doOnNext(auth -> log.info("Converted authentication: {}", auth))
                .flatMap(authentication -> {
                    if (authentication instanceof JwtAccessTokenAuthentication) {
                        return Mono.just(authenticationManager);
                    } else if (authentication instanceof JwtRefreshTokenAuthentication) {
                        return Mono.just(refreshTokenAuthenticationManager);
                    }
                    return Mono.error(new IllegalArgumentException("Unsupported Authentication Type"));
                });
    }
}
