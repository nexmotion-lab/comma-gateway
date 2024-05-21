package com.coders.commagateway.security;

import com.coders.commagateway.security.exception.ClaimEmailNotFoundException;
import com.coders.commagateway.security.exception.ClaimRoleNotFoundException;
import com.coders.commagateway.security.exception.CreateTokenException;
import com.coders.commagateway.security.exception.InvalidTokenException;
import com.coders.commagateway.security.jwt.JwtCreateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class RefreshTokenAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtCreateService jwtCreateService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .filter(auth -> (auth instanceof JwtRefreshTokenAuthentication))
                .cast(JwtRefreshTokenAuthentication.class)
                .flatMap(auth -> {
                    log.info("인증시작");
                    if (!auth.isAuthenticated()) {
                        return Mono.error(new InvalidTokenException("Authentication is not Verify"));
                    }

                    if (auth.getPrincipal() == null) {
                        return Mono.error(new ClaimEmailNotFoundException("Email is not found"));
                    }
                    return jwtCreateService.createRefreshAndAccessToken(auth.getPrincipal().toString(), auth.getCredentials().toString())
                            .doOnNext(response -> log.info("생성된 엑세스 토큰: {}, 리프레시 토큰: {}", response.getAccessToken(), response.getRefreshToken()))
                            .flatMap(response -> {

                                if (response.getAccessToken() == null || response.getRefreshToken() == null) {
                                    return Mono.error(new CreateTokenException("IPC Error"));
                                }

                                auth.setReissuanceToken(response);

                                return Mono.just(auth);
                            })
                            .onErrorResume(ex -> {
                                log.error("Error in jwtCreateService: {}", ex.getMessage(), ex);
                                return Mono.error(new AuthenticationServiceException("IPC Error", ex));
                            })
                            .switchIfEmpty(Mono
                                    .defer(() -> Mono.error(new IllegalStateException("비어있음"))));
                });
    }


}
