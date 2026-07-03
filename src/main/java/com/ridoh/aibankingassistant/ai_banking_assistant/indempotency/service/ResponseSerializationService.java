package com.ridoh.aibankingassistant.ai_banking_assistant.indempotency.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResponseSerializationService {

    private final ObjectMapper objectMapper;

    public String serialize(ApiResponse<?> response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize API response", ex);
        }
    }

    public ApiResponse<?> deserialize(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode dataNode = responseNode.get("data");
            Object data = dataNode == null || dataNode.isNull()
                    ? null
                    : objectMapper.convertValue(dataNode, Object.class);

            return ApiResponse.builder()
                    .success(responseNode.path("success").asBoolean())
                    .message(responseNode.path("message").asText())
                    .data(data)
                    .build();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize API response", ex);
        }
    }

    public <T> T deserializeData(String responseBody, Class<T> responseType) {
        ApiResponse<?> response = deserialize(responseBody);
        return objectMapper.convertValue(response.getData(), responseType);
    }
}