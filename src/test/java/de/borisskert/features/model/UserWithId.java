package de.borisskert.features.model;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

public class UserWithId {
    public static final TypeReference<List<UserWithId>> LIST_TYPE = new TypeReference<>() {
    };

    public final String id;
    public final User user;

    public UserWithId(String id, User user) {
        this.id = id;
        this.user = user;
    }

    public static UserWithId from(Map<String, String> entry) {
        String id = entry.get("ID");
        return new UserWithId(id, User.from(entry));
    }
}
