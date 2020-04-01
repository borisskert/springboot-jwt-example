package de.borisskert.springjpaliquibase;

import de.borisskert.springjpaliquibase.authentication.AppProperties;
import de.borisskert.springjpaliquibase.persistence.UserEntity;
import de.borisskert.springjpaliquibase.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

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

    public String signUp(UserWithPassword user) {
        throwIfUsernameExists(user.getUsername());

        String id = createNewId();
        UserEntity entity = user.toEntityWithId(id);

        String encryptedPassword = passwordEncoder.encode(user.getRawPassword());
        entity.setPassword(encryptedPassword);

        repository.save(entity);

        return id;
    }

    public void initializeAdmins(Collection<AppProperties.Credentials> admins) {
        admins
                .forEach(credentials -> {
                    repository.findOneByUsername(credentials.getUsername())
                            .ifPresentOrElse(
                                    user -> {
                                        LOG.debug("Admin '" + user.getUsername() + "' already exists");
                                    },
                                    () -> createAdmin(credentials)
                            );
                });
    }

    private void createAdmin(AppProperties.Credentials credentials) {
        String newId = createNewId();
        String encryptedPassword = passwordEncoder.encode(credentials.getPassword());

        UserEntity entity = credentials.toEntityWithIdAndEncryptedPassword(
                newId,
                encryptedPassword
        );

        repository.save(entity);
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
}
