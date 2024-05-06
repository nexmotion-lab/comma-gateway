package com.coders.commagateway.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.coders.commagateway.filter.FilterUtils;
import com.coders.commagateway.security.exception.InvalidTokenException;
import com.coders.commagateway.security.exception.TokenMissingException;
import com.coders.commagateway.security.jwt.JwtService;
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
public class TokenAuthenticationConverter implements ServerAuthenticationConverter {


    @Autowired
    private JwtService jwtService;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(jwtService.getAccessHeader()))
                .map(HttpCookie::getValue)
                .switchIfEmpty(Mono.error(new TokenMissingException("Token is missing")))
                .flatMap(token -> {
                    try {
                        return Mono.just(jwtService.verifyAndParseToken(token));
                    } catch (JWTVerificationException e) {
                        return Mono.error(new InvalidTokenException("Token is invalid"));
                    }
                })
                .map(JwtTokenAuthentication::new)
                .map(auth -> {
                    auth.setAuthenticated(true);
                    return auth;
                });
    }

}
