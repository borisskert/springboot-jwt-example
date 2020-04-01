package de.borisskert.features.model;

import java.time.LocalDate;
import java.util.Map;

public class UserWithPassword {

    public final String username;
    public final String email;
    public final LocalDate dateOfBirth;

    public final String rawPassword;

    private UserWithPassword(String username, String email, LocalDate dateOfBirth, String password) {
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.rawPassword = password;
    }

    public static UserWithPassword from(Map<String, String> entry) {
        String username = entry.get("Username");
        String email = entry.get("Email");
        LocalDate birthDate = LocalDate.parse(entry.get("Day of Birth"));
        String password = entry.get("Password");

        return new UserWithPassword(username, email, birthDate, password);
    }
}
