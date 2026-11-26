package com.ridoh.aibankingassistant.ai_banking_assistant.audit.controller;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.request.AuditLogSearchRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response.AuditLogDetailResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response.AuditLogSummaryResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditLogQueryService;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Admin Audit Logs",
        description = "Read-only investigation of immutable enterprise audit events"
)
public class AdminAuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    @GetMapping
    @Operation(
            summary = "Search audit logs",
            description = """
                    Searches the immutable audit trail using optional filters,
                    pagination, and allowlisted sorting. ADMIN role is required.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit logs retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid filters, pagination, or sorting",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role required",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<AuditLogSummaryResponse>>> search(
            @Valid
            @ModelAttribute
            @ParameterObject
            AuditLogSearchRequest searchRequest,

            @ParameterObject
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<AuditLogSummaryResponse> result =
                auditLogQueryService.search(searchRequest, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Audit logs retrieved successfully",
                        PageResponse.from(result)
                )
        );
    }

    @GetMapping("/{auditId}")
    @Operation(
            summary = "Get audit log details",
            description = """
                    Retrieves the complete authorized representation of one
                    immutable audit event. ADMIN role is required.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit log retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Malformed audit identifier",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Audit log not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<AuditLogDetailResponse>> findById(
            @Parameter(
                    description = "Unique audit record identifier",
                    required = true,
                    example = "6d80c973-082b-4f0d-83e8-20e50a719930"
            )
            @PathVariable
            UUID auditId
    ) {
        AuditLogDetailResponse result =
                auditLogQueryService.findById(auditId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Audit log retrieved successfully",
                        result
                )
        );
    }
}