package de.borisskert.springjwt.authentication;

import de.borisskert.springjwt.vaidation.RawPasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

class PasswordServiceTest {

    private PasswordService passwordService;
    private RawPasswordValidator validator;

    @BeforeEach
    public void setup() throws Exception {
        validator = new RawPasswordValidator();
    }

    @Nested
    class WithSeed {
        @BeforeEach
        public void setup() throws Exception {
            passwordService = new PasswordService(new Random(12345L), validator);
        }

        @Test
        public void shouldGenerateSpecificPassword() throws Exception {
            String generatedPassword = passwordService.generate();
            assertThat(generatedPassword, is(equalTo("0I429%6\\~.*6}$§R")));
        }
    }

    @Nested
    class SecureRandom {
        @BeforeEach
        public void setup() throws Exception {
            passwordService = new PasswordService(new java.security.SecureRandom(), validator);
        }

        @Test
        public void shouldGenerateValidPasswords() throws Exception {
            String generatedPassword = passwordService.generate();

            assertThat(generatedPassword, is(not(nullValue())));
            assertThat(validator.isValid(generatedPassword, null), is(equalTo(true)));
        }
    }
}
