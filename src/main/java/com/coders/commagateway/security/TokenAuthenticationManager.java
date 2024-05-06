package com.coders.commagateway.security;

import com.coders.commagateway.security.exception.*;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TokenAuthenticationManager implements ReactiveAuthenticationManager {
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .filter(auth -> (auth instanceof JwtTokenAuthentication))
                .cast(JwtTokenAuthentication.class)
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
                .cast(CustomPrincipal.class)
                .flatMap(principal -> {
                    if (principal.getEmail() == null) {
                        return Mono.error(new ClaimEmailNotFoundException("Email is not found"));
                    } else if (principal.getSocialType() == null) {
                        return Mono.error(new ClaimSocialTypeNotFoundException("SocialType is not found"));
                    }
                    return Mono.just(auth);
                })
                .onErrorMap(ClassCastException.class,
                        e -> new IllegalArgumentException("Principal cannot be cast to CustomPrincipal"));
    }

}
