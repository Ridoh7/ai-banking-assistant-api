package com.ridoh.aibankingassistant.ai_banking_assistant.indempotency.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestHashUtil {

    private static final String SHA_256 = "SHA-256";

    private final ObjectMapper objectMapper;

    public String generateHash(Object request) {
        try {
            String canonicalRequest = objectMapper.copy()
                    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                    .writeValueAsString(request);

            MessageDigest messageDigest = MessageDigest.getInstance(SHA_256);
            byte[] hashBytes = messageDigest.digest(canonicalRequest.getBytes(StandardCharsets.UTF_8));

            return toHex(hashBytes);
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to generate request hash", ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            hex.append(String.format("%02x", value));
        }

        return hex.toString();
    }
}