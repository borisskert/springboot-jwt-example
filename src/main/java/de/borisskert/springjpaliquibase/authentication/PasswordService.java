package de.borisskert.springjpaliquibase.authentication;

import de.borisskert.springjpaliquibase.vaidation.RawPasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PasswordService {
    private static final int MINIMUM_PASSWORD_LENGTH = 16;

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{};:'\"\\|`~,<>./?±§";

    private static final String[] ALLOWED_CHARACTERS = new String[]{LETTERS, DIGITS, SPECIAL};

    private final Random random;
    private final RawPasswordValidator validator;

    @Autowired
    public PasswordService(Random random, RawPasswordValidator validator) {
        this.random = random;
        this.validator = validator;
    }

    public String generate() {
        String generatedPassword;

        do {
            generatedPassword = generatePassword();
        } while (!validator.isValid(generatedPassword, null));

        return generatedPassword;
    }

    /*
     * https://stackoverflow.com/a/46156998
     */
    private String generatePassword() {
        StringBuilder builtPassword = new StringBuilder(MINIMUM_PASSWORD_LENGTH);

        for (int i = 0; i < MINIMUM_PASSWORD_LENGTH; i++) {
            int index = random.nextInt(ALLOWED_CHARACTERS.length);
            String allowedCharacters = ALLOWED_CHARACTERS[index];

            index = random.nextInt(allowedCharacters.length());
            builtPassword.append(allowedCharacters.charAt(index));
        }

        return builtPassword.toString();
    }
}
