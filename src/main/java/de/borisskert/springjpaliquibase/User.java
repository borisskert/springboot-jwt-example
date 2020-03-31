package de.borisskert.springjpaliquibase;

import de.borisskert.springjpaliquibase.persistence.UserEntity;
import de.borisskert.springjpaliquibase.vaidation.Username;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Objects;

public class User {

    @NotEmpty
    @Username
    private final String username;

    @NotEmpty
    @Email
    private final String email;

    @NotNull
    @Past
    private final LocalDate dateOfBirth;

    User(
            String username,
            String email,
            LocalDate dateOfBirth
    ) {
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public static User fromEntity(UserEntity entity) {
        return new User(
                entity.getUsername(),
                entity.getEmail(),
                entity.getDateOfBirth()
        );
    }

    public UserEntity toEntity() {
        UserEntity entity = new UserEntity();

        entity.setUsername(username);
        entity.setEmail(email);
        entity.setDateOfBirth(dateOfBirth);

        return entity;
    }

    public UserEntity toEntityWithId(String id) {
        UserEntity entity = new UserEntity();

        entity.setId(id);
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setDateOfBirth(dateOfBirth);

        return entity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username) &&
                email.equals(user.email) &&
                dateOfBirth.equals(user.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, dateOfBirth);
    }
}
