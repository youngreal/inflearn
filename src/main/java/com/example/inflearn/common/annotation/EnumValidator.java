package com.example.inflearn.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnumValidator implements ConstraintValidator<SortEnum, String> {

    private SortEnum annotation;

    @Override
    public void initialize(SortEnum constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 정렬조건 입력안해도 정상허용한다.
        if (value == null) {
            return true;
        }

        Object[] enumValues = this.annotation.enumClass().getEnumConstants();
        if (enumValues == null) {
            return false;
        }

        return Arrays.stream(enumValues)
                .anyMatch(enumValue -> value.equals(enumValue.toString()) || (this.annotation.ignoreCase() && value.equalsIgnoreCase(enumValue.toString())));
    }
}
