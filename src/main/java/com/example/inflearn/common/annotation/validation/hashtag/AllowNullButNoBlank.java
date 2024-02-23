package com.example.inflearn.common.annotation.validation.hashtag;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 해시태그는 아예 입력하지않아도 되지만, " "같은 공백입력은 허용하지않는다.
 */
@Constraint(validatedBy = HashtagEmptyValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowNullButNoBlank {
    String message() default "잘못된 해시태그 형식입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
