package com.example.musinsa.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaginationServiceTest {

    @InjectMocks
    private PaginationService sut;

    @DisplayName("페이지번호와 전체 페이지수를 입력받고 페이지에 표시될 페이지번호를 반환한다")
    @MethodSource
    @ParameterizedTest(name = "[{index}] 현재 페이지: {0}, 총 페이지: {1} => {2}")
    void 페이지에_표시될_페이지번호를_반환한다(int pageNumber, int totalPage, List<Integer> expected) {
        // given

        // when
        List<Integer> actual = sut.getPageNumbers(pageNumber, totalPage);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> 페이지에_표시될_페이지번호를_반환한다() {
        return Stream.of(
                arguments(1, 13, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
                arguments(2, 13, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
                arguments(3, 13, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
                arguments(4, 13, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
                arguments(5, 50, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
                arguments(6, 0, List.of(1)),
                arguments(10, 13, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
                arguments(11, 13, List.of(1, 1, 11, 12, 13)),
                arguments(12, 50, List.of(1, 1, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21)),
                arguments(21, 50, List.of(1, 11, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31))
        );
    }

}