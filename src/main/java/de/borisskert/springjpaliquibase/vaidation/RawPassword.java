package de.borisskert.springjpaliquibase.vaidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * https://stackoverflow.com/a/37322115
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = RawPasswordValidator.class)
@Retention(RUNTIME)
public @interface RawPassword {
    String message() default "{invalid.password}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
