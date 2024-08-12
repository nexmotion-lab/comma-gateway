package com.coders.commagateway.security;

import com.coders.commagateway.security.jwt.TokenResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpResponse response = exchange.getResponse();

        return Mono.just(authentication)
                .filter(auth -> auth instanceof JwtRefreshTokenAuthentication)
                .cast(JwtRefreshTokenAuthentication.class)
                .flatMap(auth -> {
                    TokenResponse reissuanceToken = auth.getReissuanceToken();
                    StringBuilder authorizationHeader = new StringBuilder("Bearer ");

                    if (reissuanceToken.getAccessToken() != null) {
                        authorizationHeader.append(reissuanceToken.getAccessToken());
                    }

                    if (reissuanceToken.getRefreshToken() != null) {
                        if (authorizationHeader.length() > 7) {
                            authorizationHeader.append(", ");
                        }
                        authorizationHeader.append(reissuanceToken.getRefreshToken());
                    }

                    if (authorizationHeader.length() > 7) {
                        response.getHeaders().add("Authorization", authorizationHeader.toString());
                    }

                    return Mono.empty();
                })
                .then(webFilterExchange.getChain().filter(exchange));
    }
}
