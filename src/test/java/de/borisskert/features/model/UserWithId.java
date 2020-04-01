package de.borisskert.features.model;

import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class UserWithId {
    public static final TypeReference<List<UserWithId>> LIST_TYPE = new TypeReference<>() {
    };

    public final String username;
    public final String email;
    public final LocalDate dateOfBirth;
    public final String[] roles;

    public final String id;

    private UserWithId(String username, String email, LocalDate dateOfBirth, String[] roles, String id) {
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.roles = roles;
        this.id = id;
    }

    public static UserWithId from(Map<String, String> entry) {
        String username = entry.get("Username");
        String email = entry.get("Email");
        LocalDate birthDate = LocalDate.parse(entry.get("Day of Birth"));
        String[] roles = entry.get("Roles").split(",");
        String id = entry.get("ID");

        return new UserWithId(username, email, birthDate, roles, id);
    }
}
