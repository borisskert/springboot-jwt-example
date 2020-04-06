package de.borisskert.springjwt;

import de.borisskert.springjwt.vaidation.Username;
import de.borisskert.springjwt.vaidation.Uuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping("/api/users")
@Validated
public class UsersEndpoint {

    private final UserService service;

    @Autowired
    public UsersEndpoint(UserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getUsers() {
        Collection<User> allUsers = service.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable @Uuid String id) {
        return service.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(params = "username")
    public ResponseEntity<?> findByUsername(@RequestParam @Username String username) {
        return service.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyUser() {
        User myUser = service.getMyUser();
        return ResponseEntity.ok(myUser);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid User user) {
        String createdId = service.create(user);

        return ResponseEntity.created(URI.create("/api/users/" + createdId))
                .build();
    }

    @PutMapping("/{id}")
    public void insert(@PathVariable @Uuid String id, @RequestBody @Valid User user) {
        service.insert(id, user);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid UserToSignUp newUserWithPassword) {
        String createdId = service.signUp(newUserWithPassword);

        return ResponseEntity.created(URI.create("/api/users/" + createdId))
                .build();
    }
}
