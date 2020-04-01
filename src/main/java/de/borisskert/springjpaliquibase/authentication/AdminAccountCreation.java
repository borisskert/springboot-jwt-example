package de.borisskert.springjpaliquibase.authentication;

import de.borisskert.springjpaliquibase.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountCreation implements ApplicationRunner {
    private final AppProperties properties;
    private final UserService userService;

    @Autowired
    public AdminAccountCreation(AppProperties properties, UserService userService) {
        this.properties = properties;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        userService.initializeAdmins(properties.getAdmins());
    }
}
