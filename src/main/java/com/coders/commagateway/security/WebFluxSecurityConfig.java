package com.coders.commagateway.security;

import com.coders.commagateway.security.config.CorsConfig;
import com.coders.commagateway.security.config.SecurityConfig;
import com.coders.commagateway.security.exception.ClaimNotFoundException;
import com.coders.commagateway.security.exception.InvalidTokenException;
import com.coders.commagateway.security.exception.TokenMissingException;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
@AllArgsConstructor
public class WebFluxSecurityConfig {

    private final TokenReactiveAuthenticationManagerResolver tokenReactiveAuthenticationManagerResolver;
    private final TokenAuthenticationConverter tokenAuthenticationConverter;
    private final JwtAuthenticationSuccessHandler successHandler;
    private final CorsConfig corsConfig;
    private final SecurityConfig securityConfig;

    @Bean
    @RefreshScope
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .exceptionHandling()
                .authenticationEntryPoint((exchange, ex) -> {
                    exchange.getResponse().getHeaders().add("X-Redirect", "login");
                    return writeErrorResponse(exchange.getResponse(), HttpStatus.FOUND, ex.getMessage());
                })
                .and()
                .cors().configurationSource(corsConfig.corsConfigurationSource())
                .and()
                .csrf().disable()
                .formLogin().disable();

        ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchange = http.authorizeExchange();

        securityConfig.getPathMatchers().getPermitAll().forEach(path ->
                authorizeExchange.pathMatchers(path).permitAll()
        );

        securityConfig.getRoles().forEach((role, roleConfig) -> {
            roleConfig.getPaths().forEach(path ->
                    authorizeExchange.pathMatchers(path).hasAuthority("ROLE_" + role.toUpperCase())
            );
        });

        return authorizeExchange
                .anyExchange().authenticated()
                .and()
                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    public Mono<Void> writeErrorResponse(ServerHttpResponse response, HttpStatus status, String errorMessage) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = "{\"error\":\"" + errorMessage + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private AuthenticationWebFilter authenticationWebFilter() {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(tokenReactiveAuthenticationManagerResolver);
        filter.setServerAuthenticationConverter(tokenAuthenticationConverter);
        filter.setAuthenticationFailureHandler(this::authenticationFailureHandler);
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**"));
        return filter;
    }

    private Mono<Void> authenticationFailureHandler(WebFilterExchange exchange, AuthenticationException ex) {
        if (ex instanceof TokenMissingException) {
            exchange.getExchange().getResponse().getHeaders().add("X-Redirect", "login");
            return writeErrorResponse(exchange.getExchange().getResponse(), HttpStatus.FOUND, ex.getMessage());
        } else if (ex instanceof InvalidTokenException) {
            exchange.getExchange().getResponse().getHeaders().add("X-Redirect", "login");
            return writeErrorResponse(exchange.getExchange().getResponse(), HttpStatus.UNAUTHORIZED, ex.getMessage());
        } else if (ex instanceof ClaimNotFoundException) {
            exchange.getExchange().getResponse().getHeaders().add("X-Redirect", "login");
            return writeErrorResponse(exchange.getExchange().getResponse(), HttpStatus.UNAUTHORIZED, ex.getMessage());
        } else {
            exchange.getExchange().getResponse().getHeaders().add("X-Redirect", "login");
            return writeErrorResponse(exchange.getExchange().getResponse(), HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }

}
