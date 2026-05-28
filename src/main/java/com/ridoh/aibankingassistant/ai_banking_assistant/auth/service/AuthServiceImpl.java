package com.ridoh.aibankingassistant.ai_banking_assistant.auth.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.AdminRegisterRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.AuthResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.LoginRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.RegisterRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.config.AdminProperties;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.ConflictException;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.ForbiddenException;
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

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        User savedUser = createUser(request.getFullName(), request.getEmail(), request.getPassword(), Role.USER);
        String token = jwtService.generateToken(savedUser);

        return buildAuthResponse(savedUser, token);
    }

    @Override
    @Transactional
    public AuthResponse registerAdmin(AdminRegisterRequest request) {
        // ADMIN can only be assigned through this guarded registration path.
        validateAdminSecret(request.getAdminSecret());
        User savedUser = createUser(request.getFullName(), request.getEmail(), request.getPassword(), Role.ADMIN);
        String token = jwtService.generateToken(savedUser);

        return buildAuthResponse(savedUser, token);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String token = jwtService.generateToken(user);

        return buildAuthResponse(user, token);
    }

    private User createUser(String fullName, String email, String rawPassword, Role role) {
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
        byte[] expected = adminProperties.secret().getBytes(StandardCharsets.UTF_8);
        byte[] provided = providedSecret.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(expected, provided)) {
            throw new ForbiddenException("Invalid admin secret");
        }
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType(TOKEN_TYPE)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}