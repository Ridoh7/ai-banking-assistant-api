package com.ridoh.aibankingassistant.ai_banking_assistant.audit.filter;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.context.AuditContextProviderImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Establishes stable tracing identifiers before authentication and business
 * processing begin, then exposes those identifiers to API clients.
 */
@Component
@RequiredArgsConstructor
public class AuditRequestTracingFilter extends OncePerRequestFilter {

    private final AuditContextProviderImpl auditContextProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        UUID correlationId =
                auditContextProvider.resolveCorrelationId(request);
        UUID requestId =
                auditContextProvider.resolveRequestId(request);

        response.setHeader(
                AuditContextProviderImpl.CORRELATION_ID_HEADER,
                correlationId.toString()
        );
        response.setHeader(
                AuditContextProviderImpl.REQUEST_ID_HEADER,
                requestId.toString()
        );

        filterChain.doFilter(request, response);
    }
}