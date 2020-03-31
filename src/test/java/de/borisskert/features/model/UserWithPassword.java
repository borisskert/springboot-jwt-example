package de.borisskert.features.model;

import java.util.Map;

public class UserWithPassword {

    public final String rawPassword;
    public final User user;

    private UserWithPassword(String password, User user) {
        this.rawPassword = password;
        this.user = user;
    }

    public static UserWithPassword from(Map<String, String> entry) {
        String password = entry.get("Password");
        return new UserWithPassword(password, User.from(entry));
    }
}
