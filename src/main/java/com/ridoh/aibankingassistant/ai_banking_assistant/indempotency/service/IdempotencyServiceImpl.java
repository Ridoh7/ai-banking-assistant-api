package com.ridoh.aibankingassistant.ai_banking_assistant.indempotency.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.DuplicateIdempotencyKeyException;
import java.util.Optional;

import com.ridoh.aibankingassistant.ai_banking_assistant.indempotency.entity.IdempotencyKey;
import com.ridoh.aibankingassistant.ai_banking_assistant.indempotency.repository.IdempotencyKeyRepository;
import com.ridoh.aibankingassistant.ai_banking_assistant.indempotency.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private static final String DUPLICATE_KEY_MESSAGE = "Idempotency key has already been used for a different request";

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final RequestHashUtil requestHashUtil;
    private final ResponseSerializationService responseSerializationService;

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String idempotencyKey) {
        return idempotencyKeyRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApiResponse<?>> findStoredResponse(String idempotencyKey, String endpoint, Object request) {
        String requestHash = requestHashUtil.generateHash(request);

        return idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey)
                .map(storedKey -> {
                    validateSameRequest(storedKey, endpoint, requestHash);
                    return responseSerializationService.deserialize(storedKey.getResponseBody());
                });
    }

    @Override
    @Transactional
    public void saveSuccessfulResponse(
            String idempotencyKey,
            String endpoint,
            Object request,
            ApiResponse<?> response
    ) {
        if (!response.isSuccess()) {
            return;
        }

        String requestHash = requestHashUtil.generateHash(request);
        IdempotencyKey idempotencyKeyEntity = IdempotencyKey.builder()
                .idempotencyKey(idempotencyKey)
                .endpoint(endpoint)
                .requestHash(requestHash)
                .responseBody(responseSerializationService.serialize(response))
                .build();

        try {
            idempotencyKeyRepository.saveAndFlush(idempotencyKeyEntity);
        } catch (DataIntegrityViolationException ex) {
            IdempotencyKey storedKey = idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> ex);
            validateSameRequest(storedKey, endpoint, requestHash);
        }
    }

    private void validateSameRequest(IdempotencyKey storedKey, String endpoint, String requestHash) {
        if (!storedKey.getEndpoint().equals(endpoint) || !storedKey.getRequestHash().equals(requestHash)) {
            throw new DuplicateIdempotencyKeyException(DUPLICATE_KEY_MESSAGE);
        }
    }
}