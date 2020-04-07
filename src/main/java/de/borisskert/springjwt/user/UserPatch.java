package de.borisskert.springjwt.user;

import de.borisskert.springjwt.vaidation.Username;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class UserPatch {

    @Username
    private final String username;

    @Email
    private final String email;

    @Past
    private final LocalDate dateOfBirth;

    @NotNull
    private final Set<String> roles;

    private UserPatch(String username, String email, LocalDate dateOfBirth, Set<String> roles) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPatch userPatch = (UserPatch) o;
        return Objects.equals(username, userPatch.username) &&
                Objects.equals(email, userPatch.email) &&
                Objects.equals(dateOfBirth, userPatch.dateOfBirth) &&
                Objects.equals(roles, userPatch.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, dateOfBirth, roles);
    }

    @Override
    public String toString() {
        return "UserPatch{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", roles=" + roles +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String username;
        private String email;
        private LocalDate dateOfBirth;
        private Set<String> roles = new HashSet<>();

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder role(String role) {
            this.roles.add(role);
            return this;
        }

        public UserPatch build() {
            return new UserPatch(username, email, dateOfBirth, roles);
        }
    }
}
