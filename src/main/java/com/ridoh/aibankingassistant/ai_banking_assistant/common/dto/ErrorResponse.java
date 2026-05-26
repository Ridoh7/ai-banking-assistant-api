package com.ridoh.aibankingassistant.ai_banking_assistant.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success;
    private String message;
    private String path;
    private int status;
    private LocalDateTime timestamp;
    private List<ValidationError> errors;

    public static ErrorResponse of(String message, int status, String path) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .status(status)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse validation(String message, int status, String path, List<ValidationError> errors) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .status(status)
                .path(path)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }
}
