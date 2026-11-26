package com.ridoh.aibankingassistant.ai_banking_assistant.audit.context;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditActorSnapshot;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditRequestContext;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditActorType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditSanitizationService;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.useragent.dto.UserAgentInfo;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.useragent.service.UserAgentService;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.Role;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Resolves trusted audit context from Spring Security and the active
 * server-side HTTP request.
 *
 * Actor IDs, roles, and session IDs are never accepted from request
 * parameters or request bodies.
 */
@Component
@RequiredArgsConstructor
public class AuditContextProviderImpl implements AuditContextProvider {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    private static final String CORRELATION_ID_ATTRIBUTE =
            AuditContextProviderImpl.class.getName() + ".correlationId";
    private static final String REQUEST_ID_ATTRIBUTE =
            AuditContextProviderImpl.class.getName() + ".requestId";

    private final UserAgentService userAgentService;
    private final AuditSanitizationService sanitizationService;

    @Override
    public AuditActorSnapshot resolveActor() {
        Authentication authentication = currentAuthentication();

        if (!isAuthenticated(authentication)) {
            return AuditActorSnapshot.anonymous(null);
        }

        if (authentication.getPrincipal() instanceof User user) {
            return AuditActorSnapshot.builder()
                    .actorId(user.getId())
                    .actorType(toActorType(user.getRole()))
                    .actorIdentifier(sanitizationService.maskEmail(user.getEmail()))
                    .actorRole(user.getRole().name())
                    .build();
        }

        String role = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse(null);

        return AuditActorSnapshot.builder()
                .actorType(Role.ADMIN.name().equals(role)
                        ? AuditActorType.ADMIN
                        : AuditActorType.USER)
                .actorIdentifier(
                        sanitizationService.maskEmail(authentication.getName())
                )
                .actorRole(role)
                .build();
    }

    @Override
    public AuditRequestContext resolveRequestContext() {
        Optional<HttpServletRequest> currentRequest = currentRequest();

        if (currentRequest.isEmpty()) {
            return AuditRequestContext.systemContext();
        }

        HttpServletRequest request = currentRequest.get();
        UserAgentInfo userAgentInfo = userAgentService.parse(request);

        return AuditRequestContext.builder()
                .correlationId(resolveCorrelationId(request))
                .requestId(resolveRequestId(request))
                .sessionId(resolveSessionId().orElse(null))
                .ipAddress(sanitizationService.sanitizeText(
                        request.getRemoteAddr(),
                        45
                ))
                .userAgent(sanitizationService.sanitizeUserAgent(
                        request.getHeader("User-Agent")
                ))
                .deviceType(sanitizationService.sanitizeText(
                        userAgentInfo.getDeviceClass(),
                        50
                ))
                .deviceName(sanitizationService.sanitizeText(
                        userAgentInfo.getDeviceName(),
                        150
                ))
                .browser(sanitizationService.sanitizeText(
                        userAgentInfo.getBrowser(),
                        100
                ))
                .operatingSystem(sanitizationService.sanitizeText(
                        userAgentInfo.getOperatingSystem(),
                        100
                ))
                .httpMethod(sanitizationService.sanitizeText(
                        request.getMethod(),
                        10
                ))
                .requestPath(sanitizationService.sanitizeRequestPath(
                        request.getRequestURI()
                ))
                .build();
    }

    @Override
    public Optional<UUID> resolveSessionId() {
        Authentication authentication = currentAuthentication();

        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }

        Object details = authentication.getDetails();
        return details instanceof UUID sessionId
                ? Optional.of(sessionId)
                : Optional.empty();
    }

    @Override
    public UUID resolveCorrelationId() {
        return currentRequest()
                .map(this::resolveCorrelationId)
                .orElseGet(UUID::randomUUID);
    }

    @Override
    public UUID resolveRequestId() {
        return currentRequest()
                .map(this::resolveRequestId)
                .orElseGet(UUID::randomUUID);
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated(currentAuthentication());
    }

    /**
     * Establishes or retrieves the correlation ID for a specific request.
     * This method is also reusable by the forthcoming correlation filter.
     */
    public UUID resolveCorrelationId(HttpServletRequest request) {
        return resolveRequestUuid(
                request,
                CORRELATION_ID_ATTRIBUTE,
                CORRELATION_ID_HEADER
        );
    }

    /**
     * Establishes or retrieves the unique ID for a specific HTTP request.
     */
    public UUID resolveRequestId(HttpServletRequest request) {
        return resolveRequestUuid(
                request,
                REQUEST_ID_ATTRIBUTE,
                REQUEST_ID_HEADER
        );
    }

    private UUID resolveRequestUuid(
            HttpServletRequest request,
            String attributeName,
            String headerName
    ) {
        Object existing = request.getAttribute(attributeName);
        if (existing instanceof UUID value) {
            return value;
        }

        UUID resolved = parseUuid(request.getHeader(headerName))
                .orElseGet(UUID::randomUUID);

        request.setAttribute(attributeName, resolved);
        return resolved;
    }

    private Optional<UUID> parseUuid(String value) {
        if (value == null || value.isBlank() || value.length() > 36) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Optional<HttpServletRequest> currentRequest() {
        if (RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes) {
            return Optional.of(attributes.getRequest());
        }
        return Optional.empty();
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private AuditActorType toActorType(Role role) {
        return role == Role.ADMIN
                ? AuditActorType.ADMIN
                : AuditActorType.USER;
    }
}