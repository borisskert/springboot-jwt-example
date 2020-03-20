package de.borisskert.springjpaliquibase;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class UserRepository {

    private final Map<String, User> users = new HashMap<>();

    public Optional<User> getById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<User> findByUsername(String username) {
        return users.values()
                .stream()
                .filter(user -> Objects.equals(user.getUsername(), username))
                .findFirst();
    }

    public void save(String id, User user) {
        users.put(id, user);
    }
}
