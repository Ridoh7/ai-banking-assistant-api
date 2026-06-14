package com.ridoh.aibankingassistant.ai_banking_assistant.user.controller;

import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.dto.UserResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }
}