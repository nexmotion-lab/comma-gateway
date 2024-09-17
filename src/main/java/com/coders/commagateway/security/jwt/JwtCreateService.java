package com.coders.commagateway.security.jwt;

import com.coders.commagateway.client.ApiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class JwtCreateService {

    private final ApiService apiService;

    public Mono<TokenResponse> createRefreshAndAccessToken(String email, String token) {
        TokenRequest refreshToken = new TokenRequest();
        refreshToken.setRefreshToken(token);
        return apiService.fetchDataContainBody(ApiService.AUTHENTICATION_URI,
                        "/jwt/createAccessAndRefreshToken/" + email, TokenResponse.class, refreshToken);
    }
}
