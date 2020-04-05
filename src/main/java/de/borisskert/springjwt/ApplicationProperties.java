package de.borisskert.springjwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

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

        public boolean hasPassword() {
            return password != null;
        }
    }
}
