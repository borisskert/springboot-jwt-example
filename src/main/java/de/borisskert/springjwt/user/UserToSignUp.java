package de.borisskert.springjwt.user;

import de.borisskert.springjwt.user.persistence.UserEntity;
import de.borisskert.springjwt.vaidation.RawPassword;
import de.borisskert.springjwt.vaidation.Username;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public class UserToSignUp {
    @NotEmpty
    @Username
    private final String username;

    @NotEmpty
    @Email
    private final String email;

    @NotNull
    @Past
    private final LocalDate dateOfBirth;

    @NotEmpty
    @RawPassword
    private final String rawPassword;

    public String getUsername() {
        return username;
    }

    public String getRawPassword() {
        return rawPassword;
    }

    private UserToSignUp(String username, String email, LocalDate dateOfBirth, String rawPassword) {
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.rawPassword = rawPassword;
    }

    public UserEntity toEntityWithId(String id) {
        requireNonNull(id, "[UserToSignUp] The variable 'id' must not be null");

        UserEntity entity = new UserEntity();

        entity.setId(id);
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setDateOfBirth(dateOfBirth);

        return entity;
    }

    public static UserToSignUp from(String username, String email, LocalDate dateOfBirth, String rawPassword) {
        requireNonNull(username, "[UserToSignUp] The variable 'username' must not be null");
        requireNonNull(email, "[UserToSignUp] The variable 'email' must not be null");
        requireNonNull(dateOfBirth, "[UserToSignUp] The variable 'dateOfBirth' must not be null");
        requireNonNull(rawPassword, "[UserToSignUp] The variable 'rawPassword' must not be null");

        return new UserToSignUp(username, email, dateOfBirth, rawPassword);
    }
}
