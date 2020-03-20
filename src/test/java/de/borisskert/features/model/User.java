package de.borisskert.features.model;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

public class User {

    public final String username;
    public final String email;
    public final LocalDate dateOfBirth;

    public User(String username, String email, LocalDate dateOfBirth) {
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
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

        return new User(username, email, birthDate);
    }
}
