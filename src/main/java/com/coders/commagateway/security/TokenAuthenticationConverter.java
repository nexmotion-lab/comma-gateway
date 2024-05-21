package com.coders.commagateway.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.coders.commagateway.filter.FilterUtils;
import com.coders.commagateway.security.exception.InvalidTokenException;
import com.coders.commagateway.security.exception.TokenMissingException;
import com.coders.commagateway.security.jwt.JwtService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
@Component
@AllArgsConstructor
public class TokenAuthenticationConverter implements ServerAuthenticationConverter {


    private final JwtService jwtService;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        Mono<Authentication> accessTokenVerification = Mono.justOrEmpty(exchange.getRequest()
                        .getCookies().getFirst(jwtService.getAccessHeader()))
                .map(HttpCookie::getValue)
                .flatMap(token -> {
                    try {
                        return Mono.just(jwtService.verifyAndParseAccessToken(token))
                                .map(JwtAccessTokenAuthentication::new)
                                .map(auth -> {
                                    auth.setAuthenticated(true);
                                    return auth;
                                });
                    } catch (JWTVerificationException e) {
                        return Mono.error(new InvalidTokenException("AccessToken isn't verify"));
                    }
                });

        Mono<Authentication> refreshTokenVerification = Mono.justOrEmpty(exchange.getRequest()
                .getCookies().getFirst(jwtService.getRefreshHeader()))
                .map(HttpCookie::getValue)
                .flatMap(token -> {
                    try {
                        return Mono.just(jwtService.verifyAndParseRefreshToken(token))
                                .map(JwtRefreshTokenAuthentication::new)
                                .map(auth -> {
                                    auth.setAuthenticated(true);
                                    return auth;
                                });
                    } catch (JWTVerificationException e) {
                        return Mono.error(new InvalidTokenException("RefreshToken isn't verify"));
                    }
                });

        return accessTokenVerification
                .switchIfEmpty(refreshTokenVerification)
                .switchIfEmpty(Mono.error(new TokenMissingException("Token is missing")))
                .onErrorResume(ex -> {
                    if (ex instanceof InvalidTokenException) {
                        return refreshTokenVerification
                                .switchIfEmpty(Mono.error(new InvalidTokenException("Token is missing")));
                    }
                    return Mono.error(ex);
                });
    }

}
