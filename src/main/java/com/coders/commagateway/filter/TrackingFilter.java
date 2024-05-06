package com.coders.commagateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;


@Order(1)
@Component
public class TrackingFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);

    @Autowired
    FilterUtils filterUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        Optional.ofNullable(filterUtils.getCorrelationId(requestHeaders))
                .map(cid -> {
                    logger.debug("tmx-correlation-id found in tracking filter: {}.", cid);
                    return exchange;
                })
                .orElseGet(() -> {
                    String newCorrelationId = generateCorrelationId();
                    logger.debug("tmx-correlation-id generated in tracking filter: {}.", newCorrelationId);
                    return filterUtils.setCorrelationId(exchange, newCorrelationId);
                });
        return chain.filter(exchange);
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
