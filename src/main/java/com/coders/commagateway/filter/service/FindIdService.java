package com.coders.commagateway.filter.service;

import com.coders.commagateway.client.ApiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
@Slf4j
public class FindIdService {

    private final ApiService apiService;

    public Mono<String> findAccountIdByEmail(String email) {
        return apiService.fetchDataContainBody(ApiService.AUTHENTICATION_URI,
                        "/account/findByEmail", String.class, email)
                .doOnError(ex -> log.error("API Call Failed: {}", ex.getMessage(), ex));
    }
}
