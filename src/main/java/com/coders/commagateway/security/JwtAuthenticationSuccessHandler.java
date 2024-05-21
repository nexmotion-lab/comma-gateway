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
                    if (reissuanceToken.getAccessToken() != null) {
                        ResponseCookie accessCookie = ResponseCookie.from("accessToken", reissuanceToken.getAccessToken())
                                .httpOnly(true)
                                .path("/")
                                .maxAge(3600000)
                                .build();
                        response.addCookie(accessCookie);
                    }

                    if (reissuanceToken.getRefreshToken() != null) {
                        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", reissuanceToken.getRefreshToken())
                                .httpOnly(true)
                                .path("/")
                                .maxAge(1209600000)
                                .build();
                        response.addCookie(refreshCookie);
                    }

                    return Mono.empty();
                })
                .then(webFilterExchange.getChain().filter(exchange));
    }
}
