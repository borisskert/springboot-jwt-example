package de.borisskert.springjpaliquibase.authentication;

import de.borisskert.springjpaliquibase.ApplicationProperties;
import de.borisskert.springjpaliquibase.User;
import de.borisskert.springjpaliquibase.UserService;
import de.borisskert.springjpaliquibase.UserWithPassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class AdminAccountCreation implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(AdminAccountCreation.class);

    private final ApplicationProperties properties;
    private final UserService userService;
    private final PasswordService passwordService;

    @Autowired
    public AdminAccountCreation(
            ApplicationProperties properties,
            UserService userService,
            PasswordService passwordService
    ) {
        this.properties = properties;
        this.userService = userService;
        this.passwordService = passwordService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeAdmins(properties.getAdmins());
    }

    public void initializeAdmins(Collection<ApplicationProperties.Credentials> admins) {
        admins
                .forEach(credentials -> {
                    userService.findByUsername(credentials.getUsername())
                            .ifPresentOrElse(
                                    user -> {
                                        LOG.debug("Admin '" + user.getUsername() + "' already exists");
                                    },
                                    () -> createAdmin(credentials)
                            );
                });
    }

    private void createAdmin(ApplicationProperties.Credentials credentials) {
        User user = User.adminWith(credentials);
        UserWithPassword userWithPassword;

        if (credentials.hasPassword()) {
            userWithPassword = user.withPassword(credentials.getPassword());
        } else {
            String generatedPassword = passwordService.generate();
            userWithPassword = user.withPassword(generatedPassword);

            LOG.warn("No password provided. Create admin account '" + user.getUsername() + "' with generated password '" + generatedPassword + "'");
        }

        userService.create(userWithPassword);
    }
}
