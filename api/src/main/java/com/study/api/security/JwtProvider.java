package com.study.api.security;

import static com.study.api.exception.ErrorCode.EMPTY_TOKEN_ERROR;
import static com.study.api.exception.ErrorCode.EXPIRED_TOKEN;
import static com.study.api.exception.ErrorCode.FAILED_VERIFY_SIGNATURE;
import static com.study.api.exception.ErrorCode.INVALID_TOKEN;

import com.study.api.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

    private static final String KEY_ROLES = "roles";
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1 hour

    @Value("${spring.jwt.secretKey")
    private String secretKey;

    public String generateToken(String userEmail, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(userEmail);
        claims.put(KEY_ROLES, roles);

        Date now = new Date();

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + TOKEN_EXPIRE_TIME))
            .signWith(SignatureAlgorithm.HS256, this.secretKey)
            .compact();

    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
        } catch (IllegalArgumentException e) {
            log.info("토큰은 필수입니다.", e);
            throw new CustomException(EMPTY_TOKEN_ERROR);
        } catch (ExpiredJwtException e) {
            log.info("만료된 토큰입니다.", e);
            throw new CustomException(EXPIRED_TOKEN);
        } catch (SignatureException e) {
            log.info("시그니처 검증에 실패한 토큰입니다.", e);
            throw new CustomException(FAILED_VERIFY_SIGNATURE);
        } catch (JwtException e) {
            log.info("토큰이 올바르지 않습니다.");
            throw new CustomException(INVALID_TOKEN);
        }

        return true;
    }
    public Authentication getAuthentication(String token) {
        return new UsernamePasswordAuthenticationToken(this.getUserEmail(token), "", this.getRoles(token));
    }
    public String getUserEmail(String token) {
        return this.parseClaims(token).getSubject();
    }

    public List<GrantedAuthority> getRoles(String token) {
        List<GrantedAuthority> simpRoles = new ArrayList<>();

        Object roles = this.parseClaims(token).get(KEY_ROLES);
        if (roles instanceof List<?>) {
            for (Object role : (ArrayList<?>) roles) {
                if (role instanceof String) {
                    simpRoles.add(new SimpleGrantedAuthority((String) role));
                }
            }
        }
        return simpRoles;
    }

    public Date getTokenExpireTime(String token) {
        return this.parseClaims(token).getExpiration();
    }
    private Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

}
