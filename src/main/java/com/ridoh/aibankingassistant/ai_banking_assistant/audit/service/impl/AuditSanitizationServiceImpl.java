package com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditSanitizationService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Centralized implementation for sanitizing and masking information
 * before it is persisted in the immutable audit store.
 *
 * The implementation uses an allow-safe-by-default approach:
 *
 * - Known secret-bearing property names are redacted recursively.
 * - JSON data is copied instead of mutating the caller's object.
 * - Control characters are removed from text values.
 * - Text and JSON payload sizes are bounded.
 * - Query strings are excluded from persisted request paths.
 */
@Service
public class AuditSanitizationServiceImpl
        implements AuditSanitizationService {

    private static final int MAX_USER_AGENT_LENGTH = 1_000;
    private static final int MAX_REQUEST_PATH_LENGTH = 500;
    private static final int MAX_FAILURE_REASON_LENGTH = 500;

    /**
     * Prevents audit metadata from becoming an uncontrolled document store.
     *
     * The limit applies to the serialized sanitized JSON representation.
     */
    private static final int MAX_JSON_LENGTH = 16_000;

    private static final String REDACTED_VALUE = "[REDACTED]";

    private static final Pattern CONTROL_CHARACTERS =
            Pattern.compile("[\\p{Cc}&&[^\\r\\n\\t]]");

    private static final Pattern MULTIPLE_SPACES =
            Pattern.compile("[ \\t]{2,}");

    private static final Pattern STACK_TRACE_LINE =
            Pattern.compile(
                    "(?m)^\\s*at\\s+[\\w.$]+\\([^\\r\\n]*\\)\\s*$"
            );

    private static final Pattern CAUSED_BY_LINE =
            Pattern.compile(
                    "(?im)^\\s*(caused by|suppressed):.*$"
            );

    private static final Pattern BEARER_TOKEN =
            Pattern.compile(
                    "(?i)bearer\\s+[a-z0-9._~+/=-]+"
            );

    private static final Pattern JWT_PATTERN =
            Pattern.compile(
                    "(?i)\\beyJ[a-z0-9_-]+\\.[a-z0-9_-]+\\.[a-z0-9_-]+\\b"
            );

    private static final Pattern BASIC_AUTH_PATTERN =
            Pattern.compile(
                    "(?i)basic\\s+[a-z0-9+/=]+"
            );

    private static final Set<String> PROHIBITED_JSON_KEYS = Set.of(
            "password",
            "passwordhash",
            "hashedpassword",
            "currentpassword",
            "newpassword",
            "confirmpassword",
            "pin",
            "transactionpin",
            "cvv",
            "cvc",
            "token",
            "accesstoken",
            "refreshtoken",
            "jwttoken",
            "authorization",
            "authorizationheader",
            "cookie",
            "setcookie",
            "secret",
            "secretkey",
            "apikey",
            "clientsecret",
            "privatekey",
            "encryptionkey",
            "decryptionkey",
            "credentials",
            "credential",
            "sessioncookie"
    );

    @Override
    public String sanitizeText(
            String value,
            int maximumLength
    ) {
        if (value == null) {
            return null;
        }

        if (maximumLength < 1) {
            throw new IllegalArgumentException(
                    "Maximum length must be greater than zero"
            );
        }

        String sanitized = removeUnsafeCharacters(value);
        sanitized = redactCredentialPatterns(sanitized);
        sanitized = normalizeWhitespace(sanitized);
        sanitized = sanitized.trim();

        if (sanitized.isEmpty()) {
            return null;
        }

        return truncateSafely(sanitized, maximumLength);
    }

    @Override
    public String sanitizeRequestPath(String requestPath) {
        if (requestPath == null || requestPath.isBlank()) {
            return null;
        }

        String pathWithoutQuery = extractPathWithoutQuery(requestPath);

        return sanitizeText(
                pathWithoutQuery,
                MAX_REQUEST_PATH_LENGTH
        );
    }

    @Override
    public String sanitizeUserAgent(String userAgent) {
        return sanitizeText(
                userAgent,
                MAX_USER_AGENT_LENGTH
        );
    }

    @Override
    public String sanitizeFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return null;
        }

        String sanitized = STACK_TRACE_LINE
                .matcher(failureReason)
                .replaceAll("");

        sanitized = CAUSED_BY_LINE
                .matcher(sanitized)
                .replaceAll("");

        sanitized = redactCredentialPatterns(sanitized);

        return sanitizeText(
                sanitized,
                MAX_FAILURE_REASON_LENGTH
        );
    }

    @Override
    public JsonNode sanitizeMetadata(JsonNode metadata) {
        return sanitizeJson(metadata);
    }

    @Override
    public JsonNode sanitizeState(JsonNode state) {
        return sanitizeJson(state);
    }

    @Override
    public String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String normalized = email
                .trim()
                .toLowerCase(Locale.ROOT);

        int atIndex = normalized.indexOf('@');

        if (atIndex <= 0 || atIndex == normalized.length() - 1) {
            return maskGenericIdentifier(normalized);
        }

        String localPart = normalized.substring(0, atIndex);
        String domainPart = normalized.substring(atIndex + 1);

        String visiblePrefix;

        if (localPart.length() == 1) {
            visiblePrefix = localPart.substring(0, 1);
        } else {
            visiblePrefix = localPart.substring(
                    0,
                    Math.min(2, localPart.length())
            );
        }

        return visiblePrefix + "***@" + domainPart;
    }

    @Override
    public String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return null;
        }

        String normalized = accountNumber
                .replaceAll("\\s+", "")
                .trim();

        if (normalized.length() <= 4) {
            return "*".repeat(normalized.length());
        }

        String finalFourDigits = normalized.substring(
                normalized.length() - 4
        );

        return "*".repeat(normalized.length() - 4)
                + finalFourDigits;
    }

    @Override
    public String hashSensitiveReference(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return toLowercaseHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 hashing algorithm is unavailable",
                    exception
            );
        }
    }

    /**
     * Returns a deeply sanitized copy of the supplied JSON value.
     *
     * The original JsonNode is never modified.
     */
    private JsonNode sanitizeJson(JsonNode source) {
        if (source == null || source.isNull()) {
            return null;
        }

        JsonNode sanitized = sanitizeJsonNode(source);

        if (sanitized == null || sanitized.isNull()) {
            return null;
        }

        int serializedLength = sanitized.toString().length();

        if (serializedLength > MAX_JSON_LENGTH) {
            throw new IllegalArgumentException(
                    "Audit JSON data exceeds the maximum permitted size of "
                            + MAX_JSON_LENGTH
                            + " characters"
            );
        }

        return sanitized;
    }

    private JsonNode sanitizeJsonNode(JsonNode source) {
        if (source.isObject()) {
            return sanitizeObjectNode((ObjectNode) source);
        }

        if (source.isArray()) {
            return sanitizeArrayNode((ArrayNode) source);
        }

        if (source.isTextual()) {
            String sanitizedValue = sanitizeText(
                    source.textValue(),
                    MAX_JSON_LENGTH
            );

            if (sanitizedValue == null) {
                return TextNode.valueOf("");
            }

            return TextNode.valueOf(sanitizedValue);
        }
        /*
         * Numeric, boolean, binary and other non-text scalar values
         * are copied without modification.
         */
        return source.deepCopy();
    }

    private ObjectNode sanitizeObjectNode(ObjectNode source) {
        ObjectNode sanitizedObject = source.objectNode();

        Iterator<Map.Entry<String, JsonNode>> fields =
                source.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();

            String fieldName = sanitizeJsonFieldName(
                    field.getKey()
            );

            if (fieldName == null) {
                continue;
            }

            if (isProhibitedJsonKey(fieldName)) {
                sanitizedObject.put(
                        fieldName,
                        REDACTED_VALUE
                );
                continue;
            }

            JsonNode sanitizedValue =
                    sanitizeJsonNode(field.getValue());

            sanitizedObject.set(
                    fieldName,
                    sanitizedValue
            );
        }

        return sanitizedObject;
    }

    private ArrayNode sanitizeArrayNode(ArrayNode source) {
        ArrayNode sanitizedArray = source.arrayNode();

        for (JsonNode element : source) {
            sanitizedArray.add(
                    sanitizeJsonNode(element)
            );
        }

        return sanitizedArray;
    }

    private String sanitizeJsonFieldName(String fieldName) {
        return sanitizeText(fieldName, 150);
    }

    private boolean isProhibitedJsonKey(String fieldName) {
        String normalized = normalizeJsonKey(fieldName);

        if (PROHIBITED_JSON_KEYS.contains(normalized)) {
            return true;
        }

        /*
         * Covers variations such as:
         *
         * user_password
         * access-token-value
         * refreshTokenHash
         * authorizationValue
         */
        return normalized.contains("password")
                || normalized.contains("refreshtoken")
                || normalized.contains("accesstoken")
                || normalized.contains("authorization")
                || normalized.contains("privatekey")
                || normalized.contains("clientsecret")
                || normalized.endsWith("pin")
                || normalized.endsWith("cvv")
                || normalized.endsWith("cvc");
    }

    private String normalizeJsonKey(String fieldName) {
        return fieldName
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private String extractPathWithoutQuery(String requestPath) {
        try {
            URI uri = new URI(requestPath);

            if (uri.getPath() != null && !uri.getPath().isBlank()) {
                return uri.getPath();
            }
        } catch (URISyntaxException ignored) {
            /*
             * Some servlet request values are relative paths rather
             * than complete URIs. Fall back to safe delimiter removal.
             */
        }

        int queryIndex = requestPath.indexOf('?');
        int fragmentIndex = requestPath.indexOf('#');

        int cutoffIndex = requestPath.length();

        if (queryIndex >= 0) {
            cutoffIndex = Math.min(cutoffIndex, queryIndex);
        }

        if (fragmentIndex >= 0) {
            cutoffIndex = Math.min(cutoffIndex, fragmentIndex);
        }

        return requestPath.substring(0, cutoffIndex);
    }

    private String removeUnsafeCharacters(String value) {
        return CONTROL_CHARACTERS
                .matcher(value)
                .replaceAll("");
    }

    private String normalizeWhitespace(String value) {
        String normalizedLineBreaks = value
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        return MULTIPLE_SPACES
                .matcher(normalizedLineBreaks)
                .replaceAll(" ");
    }

    private String redactCredentialPatterns(String value) {
        String sanitized = BEARER_TOKEN
                .matcher(value)
                .replaceAll("Bearer " + REDACTED_VALUE);

        sanitized = BASIC_AUTH_PATTERN
                .matcher(sanitized)
                .replaceAll("Basic " + REDACTED_VALUE);

        return JWT_PATTERN
                .matcher(sanitized)
                .replaceAll(REDACTED_VALUE);
    }

    /**
     * Truncates without splitting a UTF-16 surrogate pair.
     */
    private String truncateSafely(
            String value,
            int maximumLength
    ) {
        if (value.length() <= maximumLength) {
            return value;
        }

        int endIndex = maximumLength;

        if (endIndex > 0
                && Character.isHighSurrogate(
                value.charAt(endIndex - 1)
        )) {
            endIndex--;
        }

        return value.substring(0, endIndex);
    }

    private String maskGenericIdentifier(String identifier) {
        if (identifier.length() <= 2) {
            return "*".repeat(identifier.length());
        }

        return identifier.substring(0, 2) + "***";
    }

    private String toLowercaseHex(byte[] bytes) {
        StringBuilder result =
                new StringBuilder(bytes.length * 2);

        for (byte currentByte : bytes) {
            result.append(
                    Character.forDigit(
                            (currentByte >> 4) & 0xF,
                            16
                    )
            );

            result.append(
                    Character.forDigit(
                            currentByte & 0xF,
                            16
                    )
            );
        }

        return result.toString();
    }
}