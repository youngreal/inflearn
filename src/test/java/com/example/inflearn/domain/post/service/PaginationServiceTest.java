package com.example.inflearn.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PaginationServiceTest {

    private PaginationService sut;

    @BeforeEach
    void setup() {
        sut = new PaginationService();
    }

    @DisplayName("게시글 검색을 위한 offSet을 계산한다")
    @MethodSource
    @ParameterizedTest
    void offset_when_searchPost(int input, int expected) {
        // given& when
        int actual = sut.calculateOffSet(input);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> offset_when_searchPost() {
        return Stream.of(
                arguments(1,0),
                arguments(5,80),
                arguments(10,180),
                arguments(11,200),
                arguments(15,280)
        );
    }

    @DisplayName("페이지번호를 구하기위한 size를 계산한다")
    @Test
    void size_when_getPageNumbers() {
        // given
        int size = 20;

        // when
        int actual = sut.sizeForTotalPageNumbers(size);

        // then
        assertThat(actual).isEqualTo(200);
    }


    @DisplayName("페이지번호를 구하기위한 offset을 계산한다")
    @MethodSource
    @ParameterizedTest
    void offset_when_getPageNumbers(int input, int expected) {
        // given & when
        int actual = sut.offsetForTotalPageNumbers(input);

        // then
        assertThat(actual).isEqualTo(expected);
    }
    static Stream<Arguments> offset_when_getPageNumbers() {
        return Stream.of(
                arguments(3,0),
                arguments(1,0),
                arguments(5,0),
                arguments(10,0),
                arguments(11,200),
                arguments(20,200),
                arguments(21,400),
                arguments(30,400),
                arguments(100,1800),
                arguments(101,2000),
                arguments(102,2000),
                arguments(105,2000)
        );
    }
}