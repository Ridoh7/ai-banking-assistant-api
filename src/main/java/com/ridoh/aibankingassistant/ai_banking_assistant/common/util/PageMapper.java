package com.ridoh.aibankingassistant.ai_banking_assistant.common.util;

import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.PageResponse;
import org.springframework.data.domain.Page;

public final class PageMapper {

    private PageMapper() {
    }

    public static <T> PageResponse<T> from(Page<T> page) {

        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}