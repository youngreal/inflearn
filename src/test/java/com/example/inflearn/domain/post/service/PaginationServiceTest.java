package com.example.inflearn.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PaginationServiceTest {

    private PaginationService sut;

    @BeforeEach
    void setup() {
        sut = new PaginationService();
    }

    @MethodSource
    @ParameterizedTest
    void 게시글_검색을_위해_offset을_계산한다(int input, int expected) {
        // given& when
        int actual = sut.calculateOffSet(input);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> 게시글_검색을_위해_offset을_계산한다() {
        return Stream.of(
                arguments(1,0),
                arguments(5,80),
                arguments(10,180),
                arguments(11,200),
                arguments(15,280)
        );
    }

    @Test
    void 페이지_번호를_구하기_위해_size를_계산한다() {
        // given
        int size = 20;

        // when
        int actual = sut.sizeForTotalPageNumbers(size);

        // then
        assertThat(actual).isEqualTo(200);
    }


    @MethodSource
    @ParameterizedTest
    void 페이지_번호를_구하기_위해_offset을_계산한다(int input, int expected) {
        // given & when
        int actual = sut.offsetForTotalPageNumbers(input);

        // then
        assertThat(actual).isEqualTo(expected);
    }
    static Stream<Arguments> 페이지_번호를_구하기_위해_offset을_계산한다() {
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