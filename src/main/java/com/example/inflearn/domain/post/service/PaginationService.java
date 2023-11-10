package com.example.inflearn.domain.post.service;

import org.springframework.stereotype.Component;

@Component
public class PaginationService {

    private static final int POST_QUANTITY_PER_PAGE = 20;
    private static final int PAGE_TOTAL_NUMBER_PER_VIEW = 10;

    public int calculateOffSet(int page) {
        return (page - 1) * POST_QUANTITY_PER_PAGE;
    }

    public int sizeForTotalPageNumbers(int size) {
        return size * PAGE_TOTAL_NUMBER_PER_VIEW;
    }

    public int offsetForTotalPageNumbers(int pageNumber) {
        int offset;

        if (pageNumber % PAGE_TOTAL_NUMBER_PER_VIEW == 0) {
            offset = (pageNumber - PAGE_TOTAL_NUMBER_PER_VIEW) * POST_QUANTITY_PER_PAGE;
        } else {
            offset = (pageNumber - (pageNumber % PAGE_TOTAL_NUMBER_PER_VIEW)) * POST_QUANTITY_PER_PAGE;
        }

        return offset;
    }
}
