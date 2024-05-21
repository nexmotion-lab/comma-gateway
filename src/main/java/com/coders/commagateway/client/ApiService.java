package com.coders.commagateway.client;

import com.coders.commagateway.security.jwt.TokenResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class ApiService {

    private final WebClient.Builder webClientBuilder;

    public static String AUTHENTICATION_URI = "authentication";


    public <T> Mono<T> fetchData(String serviceId, String path, Class<T> returnType) {
        WebClient webClient = webClientBuilder.baseUrl("lb://" + serviceId).build();

        return webClient.post()
                .uri(path)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new RuntimeException("4xx Client Error"))
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new RuntimeException("5xx Client Error"))
                )
                .bodyToMono(returnType);
    }

    public <T, S> Mono<T> fetchDataContainBody(String serviceId, String path, Class<T> returnType, S data) {
        WebClient webClient = webClientBuilder.baseUrl("lb://" + serviceId).build();

        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new RuntimeException("4xx Client Error"))
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new RuntimeException("5xx Client Error"))
                )
                .bodyToMono(returnType)
                .doOnNext(response -> log.info("API Response: {}", response))
                .doOnError(ex -> log.error("API Call Failed: {}", ex.getMessage(), ex));
    }
}
