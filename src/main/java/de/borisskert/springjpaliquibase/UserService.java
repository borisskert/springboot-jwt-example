package de.borisskert.springjpaliquibase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> getUserById(@PathVariable String id) {
        return repository.getById(id);
    }

    public Optional<User> findByUsername(@RequestParam String username) {
        return repository.findByUsername(username);
    }

    public String create(@RequestBody @Valid User user) {
        throwIfUsernameExists(user.getUsername());

        String id = createNewId();
        repository.save(id, user);

        return id;
    }

    public void insert(@PathVariable String id, @RequestBody @Valid User user) {
        throwIfIdExists(id);
        throwIfUsernameExists(user.getUsername());

        repository.save(id, user);
    }

    private String createNewId() {
        return UUID.randomUUID().toString();
    }

    private void throwIfIdExists(String id) {
        if (repository.getById(id).isPresent()) {
            throw new UsernameAlreadyExistsException("Id '" + id + "' already exists");
        }
    }

    private void throwIfUsernameExists(String username) {
        if (repository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException("Username '" + username + "' already exists");
        }
    }
}
