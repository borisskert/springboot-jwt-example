package de.borisskert.springjwt.user;

import de.borisskert.springjwt.user.exception.UserAlreadyExistsException;
import de.borisskert.springjwt.user.persistence.UserEntity;
import de.borisskert.springjwt.user.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Collection<User> getAllUsers() {
        List<UserEntity> entities = repository.findAll();
        return entities.stream()
                .map(User::fromEntity)
                .collect(Collectors.toUnmodifiableList());
    }

    public Optional<User> getUserById(@PathVariable String id) {
        return repository.findById(id)
                .map(User::fromEntity);
    }

    public Optional<User> findByUsername(@RequestParam String username) {
        return repository.findOneByUsername(username)
                .map(User::fromEntity);
    }

    public String create(User user) {
        throwIfUsernameExists(user.getUsername());

        String id = createNewId();
        UserEntity entity = user.toEntityWithId(id);

        repository.save(entity);

        return id;
    }

    public void insert(String id, User user) {
        throwIfIdExists(id);
        throwIfUsernameExists(user.getUsername());

        UserEntity entity = user.toEntityWithId(id);

        repository.save(entity);
    }

    public String signUp(UserToSignUp user) {
        throwIfUsernameExists(user.getUsername());

        String id = createNewId();
        UserEntity entity = user.toEntityWithId(id);

        String encryptedPassword = passwordEncoder.encode(user.getRawPassword());
        entity.setPassword(encryptedPassword);
        entity.setRoles(List.of("USER"));

        repository.save(entity);

        return id;
    }

    public String create(UserWithPassword user) {
        throwIfUsernameExists(user.getUsername());

        String id = createNewId();
        UserEntity entity = user.toEntityWithId(id);

        String encryptedPassword = passwordEncoder.encode(user.getRawPassword());
        entity.setPassword(encryptedPassword);

        repository.save(entity);

        return id;
    }

    private String createNewId() {
        return UUID.randomUUID().toString();
    }

    private void throwIfIdExists(String id) {
        if (repository.findById(id).isPresent()) {
            throw new UserAlreadyExistsException("Id '" + id + "' already exists");
        }
    }

    private void throwIfUsernameExists(String username) {
        if (repository.findOneByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username '" + username + "' already exists");
        }
    }

    public User getMyUser() {
        return null;
    }

    public void patch(String userId, UserPatch patch) {

    }
}
