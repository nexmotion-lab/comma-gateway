package com.coders.commagateway.security;

import com.coders.commagateway.security.exception.*;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Primary
public class AccessTokenAuthenticationManager implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .filter(auth -> (auth instanceof JwtAccessTokenAuthentication))
                .cast(JwtAccessTokenAuthentication.class)
                .flatMap(auth -> {
                    if (!auth.isAuthenticated()) {
                        return Mono.error(new InvalidTokenException("Authentication is not Verify"));
                    }
                    return authCheck(auth);
                });
    }

    public Mono<? extends Authentication> authCheck(JwtTokenAuthentication auth) {
        if (auth.getAuthorities().isEmpty()) {
            return Mono.error(new ClaimRoleNotFoundException("Role is not found"));
        }

        return Mono.just(auth.getPrincipal())
                .flatMap(principal -> {
                    if (principal == null) {
                        return Mono.error(new ClaimEmailNotFoundException("Email is not found"));
                    }
                    return Mono.just(auth);
                });
    }
}
