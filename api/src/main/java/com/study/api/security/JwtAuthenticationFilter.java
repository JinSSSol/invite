package com.study.api.security;

import static com.study.api.exception.ErrorCode.INVALID_TOKEN_IN_HEADER;

import com.study.api.exception.CustomException;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        log.info("REQUEST [ SERVLET_PATH : {} ]", request.getServletPath());

        String tokenHeader = request.getHeader(TOKEN_HEADER);
        if (!ObjectUtils.isEmpty(tokenHeader)) {
            String token = this.resolveTokenFromHeader(tokenHeader);

            if (jwtProvider.validateToken(token)) {
                SecurityContextHolder.getContext()
                    .setAuthentication(jwtProvider.getAuthentication(token));
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveTokenFromHeader(String header) {
        if (!header.startsWith(TOKEN_PREFIX)) {
            throw new CustomException(INVALID_TOKEN_IN_HEADER);
        }
        return header.substring(TOKEN_PREFIX.length());
    }
}
