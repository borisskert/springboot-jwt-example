package de.borisskert.springjpaliquibase.vaidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * https://stackoverflow.com/a/37322115
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = {})
@Retention(RUNTIME)
@Size(min = 6, max = 12)
public @interface Username {
    String message() default "{invalid.username}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
