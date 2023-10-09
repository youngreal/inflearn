package com.example.inflearn.domain.post.service;

import org.springframework.stereotype.Component;

@Component
public class PaginationService {

    private static final int COUNT_PER_PAGE = 20;
    private static final int NUMBER_OF_PAGE = 10;

    public int calculateOffSet(int page) {
        return (page - 1) * COUNT_PER_PAGE;
    }

    public int sizeWhenGetPageNumbers(int size) {
        return size * NUMBER_OF_PAGE;
    }

    public int OffsetWhenGetPageNumbers(int page) {
        int offset;

        if (page % NUMBER_OF_PAGE == 0) {
            offset = (page - NUMBER_OF_PAGE) * COUNT_PER_PAGE;
        } else {
            offset = (page - (page % NUMBER_OF_PAGE)) * COUNT_PER_PAGE;
        }

        return offset;
    }
}
