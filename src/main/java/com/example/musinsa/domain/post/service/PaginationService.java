package com.example.musinsa.domain.post.service;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class PaginationService {
    private static final int TOTAL_NUMBER_PER_PAGE = 10;

    public List<Integer> getPageNumbers(int pageNumber, int totalPages) {
        int startNumber = Math.max(pageNumber - (TOTAL_NUMBER_PER_PAGE / 2), 0);
        int endNumber = Math.min(startNumber + TOTAL_NUMBER_PER_PAGE, totalPages);
        return IntStream.range(startNumber, endNumber).boxed().toList();
    }
}
