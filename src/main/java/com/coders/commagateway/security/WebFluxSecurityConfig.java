package com.coders.commagateway.security;

import com.coders.commagateway.security.exception.ClaimNotFoundException;
import com.coders.commagateway.security.exception.InvalidTokenException;
import com.coders.commagateway.security.exception.TokenMissingException;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
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

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .exceptionHandling()
                .authenticationEntryPoint((exchange, ex) -> {
                    exchange.getResponse().getHeaders().add("X-Redirect", "login");
                    return writeErrorResponse(exchange.getResponse(), HttpStatus.FOUND, ex.getMessage());
                })
                .and()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                .formLogin().disable()
                .authorizeExchange()
                .pathMatchers("/login/**").permitAll()
                .pathMatchers("/oauth2/**").permitAll()
                .pathMatchers("/token/**").permitAll()
                .pathMatchers("/home/**").permitAll()
                .anyExchange().authenticated()
                .and()
                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:8100", "https://localhost", "http://192.168.0.92:8100")); // 모든 출처 허용
        corsConfig.setMaxAge(3600L); // pre-flight cache duration
        corsConfig.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        corsConfig.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더 허용
        corsConfig.setAllowCredentials(true); // 쿠키를 포함한 요청 허용
        corsConfig.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return source;
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
