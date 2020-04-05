package de.borisskert.springjwt.vaidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * https://stackoverflow.com/a/37322115
 */
@Target({FIELD, PARAMETER})
@Constraint(validatedBy = RawPasswordValidator.class)
@Retention(RUNTIME)
public @interface RawPassword {
    String message() default "{invalid.password}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
