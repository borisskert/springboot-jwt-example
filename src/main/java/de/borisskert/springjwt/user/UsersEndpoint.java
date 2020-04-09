package de.borisskert.springjwt.user;

import de.borisskert.springjwt.vaidation.Username;
import de.borisskert.springjwt.vaidation.Uuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    private final UserService userService;
    private final MeService meService;

    @Autowired
    public UsersEndpoint(UserService userService, MeService meService) {
        this.userService = userService;
        this.meService = meService;
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getUsers() {
        Collection<User> allUsers = userService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable @Uuid String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(params = "username")
    public ResponseEntity<?> findByUsername(@RequestParam @Username String username) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMe() {
        return meService.getMe()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid User user) {
        String createdId = userService.create(user);

        return ResponseEntity.created(URI.create("/api/users/" + createdId))
                .build();
    }

    @PutMapping("/{id}")
    public void insert(@PathVariable @Uuid String id, @RequestBody @Valid User user) {
        userService.insert(id, user);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid UserToSignUp newUserWithPassword) {
        String createdId = userService.signUp(newUserWithPassword);

        return ResponseEntity.created(URI.create("/api/users/" + createdId))
                .build();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<?> patch(@PathVariable @Uuid String userId, @RequestBody @Valid UserPatch patch) {
        userService.patch(userId, patch);
        return ResponseEntity.noContent().build();
    }
}
