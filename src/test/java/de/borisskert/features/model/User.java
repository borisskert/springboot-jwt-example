package de.borisskert.features.model;

import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class User implements Comparable<User> {
    public static final TypeReference<List<User>> LIST_TYPE = new TypeReference<>() {
    };

    public final String username;
    public final String email;
    public final LocalDate dateOfBirth;
    public final String[] roles;

    private User(String username, String email, LocalDate dateOfBirth, String[] roles) {
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.roles = roles;
    }

    @Override
    public int compareTo(User other) {
        return Comparator.<User, String>comparing(user -> user.username)
                .thenComparing(user -> user.email)
                .thenComparing(user -> user.dateOfBirth)
                .compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(email, user.email) &&
                Objects.equals(dateOfBirth, user.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, dateOfBirth);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }

    public static User from(Map<String, String> entry) {
        String username = entry.get("Username");
        String email = entry.get("Email");
        LocalDate birthDate = LocalDate.parse(entry.get("Day of Birth"));
        String[] roles = entry.get("Roles").split(",");

        return new User(username, email, birthDate, roles);
    }
}
