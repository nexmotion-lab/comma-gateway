package com.coders.commagateway.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    public static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    public static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    public static final String EMAIL_CLAIM = "email";
    public static final String SOCIAL_TYPE_CLAIM = "socialType";
    public static final String BEARER = "Bearer ";
    public static final String ROLE = "Role";

    public DecodedJWT decodedJWT(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC512(secretKey)).build();
        return verifier.verify(token);
    }

    public DecodedJWT verifyAndParseAccessToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC512(secretKey))
                    .withSubject(ACCESS_TOKEN_SUBJECT)
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException ex) {
            log.info("AccessToken fail valid");
            throw ex;
        }
    }
    public DecodedJWT verifyAndParseRefreshToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC512(secretKey))
                    .withSubject(REFRESH_TOKEN_SUBJECT)
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException ex) {
            log.info("RefreshToken fail valid");
            throw ex;
        }
    }

}
