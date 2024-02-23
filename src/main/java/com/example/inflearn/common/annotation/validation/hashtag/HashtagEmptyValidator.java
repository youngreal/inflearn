package com.example.inflearn.common.annotation.validation.hashtag;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class HashtagEmptyValidator implements ConstraintValidator<AllowNullButNoBlank, Set<String>> {

    @Override
    public boolean isValid(Set<String> hashtags, ConstraintValidatorContext context) {
        if (hashtags == null) {
            return true;
        }
        return hashtags.stream().noneMatch(String::isBlank);
    }
}
