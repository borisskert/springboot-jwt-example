package de.borisskert.features.model;

import java.util.Map;

public class Credentials {
    public final String username;
    public final String password;

    private Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static Credentials from(Map<String, String> entry) {
        String username = entry.get("Username");
        String password = entry.get("Password");

        return new Credentials(username, password);
    }
}
