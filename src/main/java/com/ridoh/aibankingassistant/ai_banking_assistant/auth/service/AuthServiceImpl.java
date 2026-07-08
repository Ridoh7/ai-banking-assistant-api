package com.ridoh.aibankingassistant.ai_banking_assistant.auth.service;

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

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {

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

        return buildAuthResponse(
                savedUser,
                accessToken,
                refreshToken.getToken()
        );
    }

    @Override
    @Transactional
    public AuthResponse registerAdmin(AdminRegisterRequest request) {

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

        return buildAuthResponse(
                savedUser,
                accessToken,
                refreshToken.getToken()
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

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

        return buildAuthResponse(
                user,
                accessToken,
                refreshToken.getToken()
        );
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken currentRefreshToken =
                refreshTokenService.verifyRefreshToken(
                        request.getRefreshToken()
                );

        User user = currentRefreshToken.getUser();

        refreshTokenService.revokeRefreshToken(currentRefreshToken);

        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(
                        user,
                        sessionInfoService.getCurrentSession(),
                        currentRefreshToken.getSessionId()
                );

        String accessToken =
                jwtService.generateToken(
                        user,
                        newRefreshToken.getSessionId()
                );

        return buildAuthResponse(
                user,
                accessToken,
                newRefreshToken.getToken()
        );
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
}