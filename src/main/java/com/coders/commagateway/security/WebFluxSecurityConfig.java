package com.coders.commagateway.security;

import com.coders.commagateway.security.exception.ClaimNotFoundException;
import com.coders.commagateway.security.exception.InvalidTokenException;
import com.coders.commagateway.security.exception.TokenMissingException;
import com.coders.commagateway.security.jwt.JwtService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
public class WebFluxSecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authenticationManager(new TokenAuthenticationManager())
                .exceptionHandling()
                .authenticationEntryPoint((exchange, ex) -> {
                    exchange.getResponse().getHeaders().add("X-Redirect", "login");
                    return writeErrorResponse(exchange.getResponse(), HttpStatus.FOUND, ex.getMessage());
                })
                .and()
                .csrf().disable()
                .formLogin().disable()
                .authorizeExchange()
                .pathMatchers("/login").permitAll()
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

    @Bean
    public ServerAuthenticationConverter authenticationConverter() {
        return new TokenAuthenticationConverter();
    }

    private AuthenticationWebFilter authenticationWebFilter() {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(authenticationManager());
        filter.setServerAuthenticationConverter(authenticationConverter());
        filter.setAuthenticationFailureHandler(this::authenticationFailureHandler);
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

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        return new TokenAuthenticationManager();
    }

}
