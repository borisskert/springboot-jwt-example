package de.borisskert.springjpaliquibase.authentication;

import de.borisskert.springjpaliquibase.persistence.UserEntity;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @Size(min = 1)
    private List<Credentials> admins = new ArrayList<>();

    public List<Credentials> getAdmins() {
        return admins;
    }

    public void setAdmins(List<Credentials> admins) {
        this.admins = admins;
    }

    public static class Credentials {

        @NotEmpty
        private String username;

        @NotEmpty
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public UserEntity toEntityWithIdAndEncryptedPassword(String id, String encryptedPassword) {
            UserEntity entity = new UserEntity();

            entity.setId(id);
            entity.setUsername(username);
            entity.setPassword(encryptedPassword);
            entity.setDateOfBirth(LocalDate.of(1970, 1, 1));
            entity.setEmail(username + "@localhost");
            entity.setRoles(List.of("ADMIN"));

            return entity;
        }
    }
}
