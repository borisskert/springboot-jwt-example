package de.borisskert.springjwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MeService {

    private final UserService userService;

    @Autowired
    public MeService(UserService userService) {
        this.userService = userService;
    }

    public Optional<User> getMe() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication.isAuthenticated()) {
            String username = (String) authentication.getPrincipal();
            return userService.findByUsername(username);
        }

        return Optional.empty();
    }
}
