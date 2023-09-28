package com.example.inflearn.domain.post.service;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
//todo @Service? @Component?
@Service
public class PaginationService {

    private static final int DEFAULT_PAGE = 1;
    private static final int TOTAL_NUMBER_PER_PAGE = 10;

    /*
    첫번째 페이지인경우
     start = 1
     end = 10
     next = 11
     11이 ui에선 next버튼

    마지막 페이지인경우(600이 한계고 595페이지 요청)
     init = 1
     prev = 581
     start = 591
     end = 600

     첫번쨰, 마지막 페이지 이외의 경우 (353페이지 요청)
     init = 1
     prev = 341
     start = 351
     end = 360
     next = 361
     */
    public List<Integer> getPageNumbers(int currentPage, int totalPage) {
        if (totalPage == 0) {
            return List.of(DEFAULT_PAGE);
        }

        // 일의 자리수
        int pageNumberUnits = currentPage % 10;
        int startPage = calculateStartPage(currentPage, pageNumberUnits);
        int endPage = calculateEndPage(currentPage, totalPage, pageNumberUnits);
        int prev = startPage - TOTAL_NUMBER_PER_PAGE;
        int next = endPage + 1;

        return pageNumbers(totalPage, startPage, endPage, prev, next);
    }

    private List<Integer> pageNumbers(int totalPage, int startPage, int endNumber, int prev, int next) {
        if (startPage == 1) {
            return IntStream.range(startPage, next + 1).boxed().toList();
        } else if (endNumber == totalPage) {
            return Stream.concat(Stream.of(DEFAULT_PAGE), Stream.concat(Stream.of(prev),
                    IntStream.range(startPage, endNumber + 1).boxed())).toList();
        }
        return Stream.concat(Stream.of(DEFAULT_PAGE),
                        Stream.concat(Stream.of(prev), IntStream.range(startPage, next + 1).boxed()))
                .toList();
    }

    private int calculateEndPage(int currentPage, int totalPage, int pageNumberUnits) {
        int endNumber = Math.min(currentPage + TOTAL_NUMBER_PER_PAGE - pageNumberUnits, totalPage);
        if (pageNumberUnits == 0) {
            endNumber = Math.min(currentPage, totalPage);
        }
        return endNumber;
    }

    private int calculateStartPage(int currentPage, int pageNumberUnits) {
        int startNumber = currentPage - pageNumberUnits + 1;
        if (pageNumberUnits == 0) {
            startNumber -= TOTAL_NUMBER_PER_PAGE;
        }
        return startNumber;
    }
}
