package de.borisskert.springjwt.user;

import de.borisskert.springjwt.ApplicationProperties;
import de.borisskert.springjwt.user.persistence.UserEntity;
import de.borisskert.springjwt.vaidation.Username;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    @NotNull
    private final Collection<String> roles;

    private User(
            String username,
            String email,
            LocalDate dateOfBirth,
            Collection<String> roles
    ) {
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.roles = roles;
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

    public Collection<String> getRoles() {
        return roles;
    }

    public static User fromEntity(UserEntity entity) {
        List<String> clonedRoles = entity.getRoles()
                .stream()
                .collect(Collectors.toUnmodifiableList());

        return new User(
                entity.getUsername(),
                entity.getEmail(),
                entity.getDateOfBirth(),
                clonedRoles
        );
    }

    public static User from(
            String username,
            String email,
            LocalDate dateOfBirth
    ) {
        return new User(
                username,
                email,
                dateOfBirth,
                List.of()
        );
    }

    public static User from(
            String username,
            String email,
            LocalDate dateOfBirth,
            Collection<String> roles
    ) {
        return new User(
                username,
                email,
                dateOfBirth,
                roles
        );
    }

    public static User adminWith(ApplicationProperties.Credentials credentials) {
        String username = credentials.getUsername();

        return new User(
                username,
                username + "@localhost",
                LocalDate.of(1970, 1, 1),
                Set.of("ADMIN")
        );
    }

    public UserWithPassword withPassword(String rawPassword) {
        return UserWithPassword.from(this, rawPassword);
    }

    public UserEntity toEntity() {
        UserEntity entity = new UserEntity();

        entity.setUsername(username);
        entity.setEmail(email);
        entity.setDateOfBirth(dateOfBirth);
        entity.setRoles(roles);

        return entity;
    }

    public UserEntity toEntityWithId(String id) {
        UserEntity entity = new UserEntity();

        List<String> clonedRoles = roles.stream()
                .collect(Collectors.toUnmodifiableList());

        entity.setId(id);
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setDateOfBirth(dateOfBirth);
        entity.setRoles(clonedRoles);

        return entity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(email, user.email) &&
                Objects.equals(dateOfBirth, user.dateOfBirth) &&
                Objects.equals(roles, user.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, dateOfBirth, roles);
    }
}
