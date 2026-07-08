package com.ridoh.aibankingassistant.ai_banking_assistant.auth.controller;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.dto.SessionResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.service.SessionManagementService;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.PageResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.util.PageMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/sessions")
@RequiredArgsConstructor
@Tag(
        name = "Session Management",
        description = "Manage authenticated user sessions"
)
public class SessionController {

    private final SessionManagementService sessionManagementService;

    @Operation(summary = "Retrieve active sessions")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SessionResponse>>> getSessions(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "lastUsedAt")
            String sortBy,

            @RequestParam(defaultValue = "DESC")
            Sort.Direction direction
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(direction, sortBy)
                );

        Page<SessionResponse> sessions =
                sessionManagementService.getCurrentUserSessions(pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Sessions retrieved successfully",
                        PageResponse.from(sessions)
                )
        );
    }

    @Operation(summary = "Logout a specific session")
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> logoutSession(
            @PathVariable UUID sessionId
    ) {

        sessionManagementService.logoutSession(sessionId);

        return ResponseEntity.ok(
                ApiResponse.success("Session logged out successfully")
        );
    }

    @Operation(summary = "Logout all other sessions")
    @DeleteMapping("/others")
    public ResponseEntity<ApiResponse<Void>> logoutOtherSessions() {

        sessionManagementService.logoutOtherSessions();

        return ResponseEntity.ok(
                ApiResponse.success("Other sessions logged out successfully")
        );
    }

    @Operation(summary = "Logout all sessions")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> logoutAllSessions() {

        sessionManagementService.logoutAllSessions();

        return ResponseEntity.ok(
                ApiResponse.success("All sessions logged out successfully")
        );
    }
}