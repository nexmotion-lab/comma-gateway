package com.coders.commagateway.security.oauth;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@AllArgsConstructor
@Slf4j
public class KeycloakAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(oauthToken.getAuthorizedClientRegistrationId())
                .principal(oauthToken)
                .attribute(ServerWebExchange.class.getName(), webFilterExchange.getExchange())
                .build();


        return authorizedClientManager.authorize(authorizeRequest)
                .flatMap(authorizedClient -> {
                    OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                    String refreshToken = authorizedClient.getRefreshToken() != null ? authorizedClient.getRefreshToken().getTokenValue() : null;

                    log.info("액세스 토큰: {}, 리프레시 토큰: {}", accessToken.getTokenValue(), refreshToken);

                    ServerWebExchange exchange = webFilterExchange.getExchange();
                    exchange.getResponse().getHeaders().setLocation(URI.create("your-app://callback?accessToken=" + accessToken.getTokenValue() + "&refreshToken=" + refreshToken));

                    return exchange.getResponse().setComplete();
                });
    }
}
