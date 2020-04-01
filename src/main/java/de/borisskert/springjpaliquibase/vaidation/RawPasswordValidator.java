package de.borisskert.springjpaliquibase.vaidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class RawPasswordValidator implements ConstraintValidator<RawPassword, String> {

    private static final int MINIMUM_PASSWORD_LENGTH = 8;
    private static final Pattern AT_LEAST_ONE_LETTER = Pattern.compile(".*[A-Za-z]+.*");
    private static final Pattern AT_LEAST_ONE_DIGIT = Pattern.compile(".*[0-9]+.*");
    private static final Pattern AT_LEAST_ONE_SPECIAL_CHARACTER = Pattern.compile(".*[±§!@#$%^&*()\\-_=+{}\\[\\];:'\"|\\\\<>,./?~`]+.*");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null
                || value.length() >= MINIMUM_PASSWORD_LENGTH
                && AT_LEAST_ONE_LETTER.matcher(value).matches()
                && AT_LEAST_ONE_DIGIT.matcher(value).matches()
                && AT_LEAST_ONE_SPECIAL_CHARACTER.matcher(value).matches();
    }
}
