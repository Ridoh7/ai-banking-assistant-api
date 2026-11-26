package com.ridoh.aibankingassistant.ai_banking_assistant.auth.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditActorSnapshot;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditEventCommand;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditResourceReference;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditActorType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditResourceType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditLogService;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditSanitizationService;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.AdminRegisterRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.AuthResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.LoginRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.RefreshTokenRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.RegisterRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.entity.RefreshToken;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.service.SessionInfoService;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.ConflictException;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.ForbiddenException;
import com.ridoh.aibankingassistant.ai_banking_assistant.config.AdminProperties;
import com.ridoh.aibankingassistant.ai_banking_assistant.security.JwtService;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.Role;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AdminProperties adminProperties;
    private final RefreshTokenService refreshTokenService;
    private final SessionInfoService sessionInfoService;
    private final AuditLogService auditLogService;
    private final AuditSanitizationService auditSanitizationService;

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        try {
            User savedUser = createUser(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPassword(),
                    Role.USER
            );

            RefreshToken refreshToken =
                    refreshTokenService.createRefreshToken(
                            savedUser,
                            sessionInfoService.getCurrentSession()
                    );

            String accessToken =
                    jwtService.generateToken(
                            savedUser,
                            refreshToken.getSessionId()
                    );

            recordAuthenticationSuccess(
                    AuditEventType.USER_REGISTRATION_SUCCESS,
                    savedUser,
                    refreshToken.getSessionId(),
                    "User registration completed successfully"
            );
            recordSessionCreated(savedUser, refreshToken);

            return buildAuthResponse(
                    savedUser,
                    accessToken,
                    refreshToken.getToken()
            );
        } catch (RuntimeException exception) {
            recordAuthenticationFailure(
                    AuditEventType.USER_REGISTRATION_FAILURE,
                    request.getEmail(),
                    "User registration failed",
                    exception
            );
            throw exception;
        }
    }

    @Override
    @Transactional
    public AuthResponse registerAdmin(AdminRegisterRequest request) {
        try {
            validateAdminSecret(request.getAdminSecret());

            User savedUser = createUser(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPassword(),
                    Role.ADMIN
            );

            RefreshToken refreshToken =
                    refreshTokenService.createRefreshToken(
                            savedUser,
                            sessionInfoService.getCurrentSession()
                    );

            String accessToken =
                    jwtService.generateToken(
                            savedUser,
                            refreshToken.getSessionId()
                    );

            recordAuthenticationSuccess(
                    AuditEventType.ADMIN_REGISTRATION_SUCCESS,
                    savedUser,
                    refreshToken.getSessionId(),
                    "Administrator registration completed successfully"
            );
            recordSessionCreated(savedUser, refreshToken);

            return buildAuthResponse(
                    savedUser,
                    accessToken,
                    refreshToken.getToken()
            );
        } catch (RuntimeException exception) {
            recordAuthenticationFailure(
                    AuditEventType.ADMIN_REGISTRATION_FAILURE,
                    request.getEmail(),
                    "Administrator registration failed",
                    exception
            );
            throw exception;
        }
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found"));

            RefreshToken refreshToken =
                    refreshTokenService.createRefreshToken(
                            user,
                            sessionInfoService.getCurrentSession()
                    );

            String accessToken =
                    jwtService.generateToken(
                            user,
                            refreshToken.getSessionId()
                    );

            recordAuthenticationSuccess(
                    AuditEventType.LOGIN_SUCCESS,
                    user,
                    refreshToken.getSessionId(),
                    "User authenticated successfully"
            );
            recordSessionCreated(user, refreshToken);

            return buildAuthResponse(
                    user,
                    accessToken,
                    refreshToken.getToken()
            );
        } catch (RuntimeException exception) {
            recordAuthenticationFailure(
                    AuditEventType.LOGIN_FAILURE,
                    request.getEmail(),
                    "User authentication failed",
                    exception
            );
            throw exception;
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        User tokenOwner = null;

        try {
            RefreshToken currentRefreshToken =
                    refreshTokenService.verifyRefreshToken(
                            request.getRefreshToken()
                    );

            tokenOwner = currentRefreshToken.getUser();

            refreshTokenService.revokeRefreshToken(currentRefreshToken);

            RefreshToken newRefreshToken =
                    refreshTokenService.createRefreshToken(
                            tokenOwner,
                            sessionInfoService.getCurrentSession(),
                            currentRefreshToken.getSessionId()
                    );

            String accessToken =
                    jwtService.generateToken(
                            tokenOwner,
                            newRefreshToken.getSessionId()
                    );

            recordAuthenticationSuccess(
                    AuditEventType.TOKEN_REFRESH_SUCCESS,
                    tokenOwner,
                    newRefreshToken.getSessionId(),
                    "Access token refreshed and refresh token rotated"
            );

            return buildAuthResponse(
                    tokenOwner,
                    accessToken,
                    newRefreshToken.getToken()
            );
        } catch (RuntimeException exception) {
            recordTokenRefreshFailure(tokenOwner, exception);
            throw exception;
        }
    }

    private User createUser(
            String fullName,
            String email,
            String rawPassword,
            Role role
    ) {

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();

        return userRepository.save(user);
    }

    private void validateAdminSecret(String providedSecret) {

        byte[] expected =
                adminProperties.secret().getBytes(StandardCharsets.UTF_8);

        byte[] provided =
                providedSecret.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(expected, provided)) {
            throw new ForbiddenException("Invalid admin secret");
        }
    }

    private AuthResponse buildAuthResponse(
            User user,
            String accessToken,
            String refreshToken
    ) {

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private void recordAuthenticationSuccess(
            AuditEventType eventType,
            User user,
            java.util.UUID sessionId,
            String description
    ) {
        auditLogService.record(
                AuditEventCommand.builder()
                        .eventType(eventType)
                        .actor(toAuditActor(user))
                        .primaryResource(AuditResourceReference.of(
                                AuditResourceType.USER,
                                user.getId().toString()
                        ))
                        .secondaryResource(AuditResourceReference.of(
                                AuditResourceType.SESSION,
                                sessionId
                        ))
                        .sessionId(sessionId)
                        .description(description)
                        .build()
        );
    }

    private void recordSessionCreated(
            User user,
            RefreshToken refreshToken
    ) {
        auditLogService.record(
                AuditEventCommand.builder()
                        .eventType(AuditEventType.SESSION_CREATED)
                        .actor(toAuditActor(user))
                        .primaryResource(AuditResourceReference.of(
                                AuditResourceType.SESSION,
                                refreshToken.getSessionId()
                        ))
                        .secondaryResource(AuditResourceReference.of(
                                AuditResourceType.USER,
                                user.getId().toString()
                        ))
                        .sessionId(refreshToken.getSessionId())
                        .description("Authenticated session created")
                        .build()
        );
    }

    private void recordAuthenticationFailure(
            AuditEventType eventType,
            String attemptedEmail,
            String description,
            RuntimeException exception
    ) {
        auditLogService.recordFailure(
                AuditEventCommand.builder()
                        .eventType(eventType)
                        .actor(AuditActorSnapshot.anonymous(
                                auditSanitizationService.maskEmail(
                                        attemptedEmail
                                )
                        ))
                        .primaryResource(AuditResourceReference.of(
                                AuditResourceType.AUTHENTICATION,
                                eventType.getAction()
                        ))
                        .description(description)
                        .failureCode(
                                exception.getClass().getSimpleName()
                        )
                        .failureReason(exception.getMessage())
                        .build()
        );
    }

    private void recordTokenRefreshFailure(
            User tokenOwner,
            RuntimeException exception
    ) {
        AuditActorSnapshot actor = tokenOwner != null
                ? toAuditActor(tokenOwner)
                : AuditActorSnapshot.anonymous(null);

        auditLogService.recordFailure(
                AuditEventCommand.builder()
                        .eventType(AuditEventType.TOKEN_REFRESH_FAILURE)
                        .actor(actor)
                        .primaryResource(AuditResourceReference.of(
                                AuditResourceType.REFRESH_TOKEN,
                                "TOKEN_REFRESH_ATTEMPT"
                        ))
                        .description("Access-token refresh failed")
                        .failureCode(
                                exception.getClass().getSimpleName()
                        )
                        .failureReason(exception.getMessage())
                        .build()
        );
    }

    private AuditActorSnapshot toAuditActor(User user) {
        return AuditActorSnapshot.builder()
                .actorId(user.getId())
                .actorType(user.getRole() == Role.ADMIN
                        ? AuditActorType.ADMIN
                        : AuditActorType.USER)
                .actorIdentifier(
                        auditSanitizationService.maskEmail(
                                user.getEmail()
                        )
                )
                .actorRole(user.getRole().name())
                .build();
    }
}