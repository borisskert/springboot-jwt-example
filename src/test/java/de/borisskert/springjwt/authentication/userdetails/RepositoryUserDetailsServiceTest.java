package de.borisskert.springjwt.authentication.userdetails;

import de.borisskert.springjwt.ApplicationProperties;
import de.borisskert.springjwt.authentication.AdminAccountCreation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@DirtiesContext
@ActiveProfiles("IT")
class RepositoryUserDetailsServiceTest {

    @Autowired
    private AdminAccountCreation adminAccountCreation;

    @Autowired
    private RepositoryUserDetailsService userDetailsService;

    @Test
    public void shouldProvideAdminUser() throws Exception {
        ApplicationProperties.Credentials credentials = new ApplicationProperties.Credentials();
        credentials.setUsername("admin");
        credentials.setPassword("admin123");

        adminAccountCreation.initializeAdmins(Set.of(credentials));

        UserDetails details = userDetailsService.loadUserByUsername("admin");

        assertThat(details.getUsername(), is(equalTo("admin")));
        assertThat(details.getAuthorities(), is(equalTo(Set.of(new SimpleGrantedAuthority("ADMIN")))));
    }
}
