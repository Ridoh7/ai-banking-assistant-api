package com.ridoh.aibankingassistant.ai_banking_assistant.security;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.entity.RefreshToken;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }

        try {

            String jwt =
                    authorizationHeader.substring(BEARER_PREFIX.length());

            String username =
                    jwtService.extractUsername(jwt);

            UUID sessionId =
                    jwtService.extractSessionId(jwt);

            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                RefreshToken session =
                        refreshTokenRepository
                                .findBySessionIdAndRevokedFalse(sessionId)
                                .orElseThrow(() ->
                                        new BadCredentialsException("Session has been revoked"));

                if (session.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
                    throw new BadCredentialsException("Session expired");
                }

                if (jwtService.validateToken(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(sessionId);

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (JwtException
                 | IllegalArgumentException
                 | AuthenticationException ex) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}