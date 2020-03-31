package de.borisskert.springjpaliquibase.vaidation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class RawPasswordValidatorTest {

    private ConstraintValidator<RawPassword, String> validator;

    @BeforeEach
    public void setup() throws Exception {
        validator = new RawPasswordValidator();
    }

    @Test
    public void shouldNotAllowEmptyPassword() throws Exception {
        assertThat(validator.isValid("", null), is(equalTo(false)));
    }

    @Test
    public void shouldNotAllowTooShortPassword() throws Exception {
        assertThat(validator.isValid("@bc123", null), is(equalTo(false)));
    }

    @Test
    public void shouldNotAllowPasswordWithoutDigits() throws Exception {
        assertThat(validator.isValid("@bcdefgh", null), is(equalTo(false)));
    }

    @Test
    public void shouldNotAllowPasswordWithoutLetters() throws Exception {
        assertThat(validator.isValid("@2345678", null), is(equalTo(false)));
    }

    @Test
    public void shouldNotAllowWithoutSpecialCharacters() throws Exception {
        assertThat(validator.isValid("a2345678", null), is(equalTo(false)));
    }

    @Test
    public void shouldAllowGoodPasswords() throws Exception {
        assertThat(validator.isValid("@bcd5678", null), is(equalTo(true)));
    }

    @Test
    public void shouldAllowNull() throws Exception {
        assertThat(validator.isValid(null, null), is(equalTo(true)));
    }
}
