package de.borisskert.springjpaliquibase;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Objects;

public class User {

    @NotEmpty
    private final String username;

    @NotEmpty
    @Email
    private final String email;

    @NotNull
    @Past
    private final LocalDate dateOfBirth;

    public User(
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
