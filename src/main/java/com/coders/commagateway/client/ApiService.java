package com.coders.commagateway.client;

import com.coders.commagateway.security.exception.InvalidTokenException;
import com.coders.commagateway.security.jwt.TokenResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@AllArgsConstructor
@Slf4j
public class ApiService {

    private final WebClient.Builder webClientBuilder;

    public static String AUTHENTICATION_URI = "authentication";

    public <T, S> Mono<T> fetchDataContainBody(String serviceId, String path, Class<T> returnType, S data) {
        WebClient webClient = webClientBuilder.baseUrl("lb://" + serviceId).build();

        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(returnType)
                .timeout(Duration.ofSeconds(3), Mono.empty())
                .onErrorResume(WebClientResponseException.NotFound.class,
                        exception -> Mono.error(new InvalidTokenException("사용자와 요청정보가 불일치합니다.")))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorResume(Exception.class,
                        exception -> Mono.empty());
    }

    public <T, S> Mono<T> getDataContainBody(String serviceId, String path, Class<T> returnType, S data, String key) {
        WebClient webClient = webClientBuilder
                .baseUrl("lb://" + serviceId)
                .build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).queryParam(key, data).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new RuntimeException("4xx Client Error"))
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new RuntimeException("5xx Client Error"))
                )
                .bodyToMono(returnType);
    }
}
