package de.borisskert.springjwt.authentication;

import de.borisskert.springjwt.FakeUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class JwtTokenServiceTest {

    private JwtTokenService service;

    @BeforeEach
    public void setup() throws Exception {
        service = new JwtTokenService();
    }

    @Test
    public void shouldAuthenticateSignedToken() throws Exception {
        FakeUserDetails userDetails = FakeUserDetails.of("admin", Set.of("ADMIN", "USER"));
        String jwt = service.createSignedTokenFor(userDetails);

        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) service.authenticate(jwt);

        assertThat(authentication.getAuthorities(), containsInAnyOrder(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        ));
        assertThat(authentication.getPrincipal(), is(equalTo("admin")));
    }
}
