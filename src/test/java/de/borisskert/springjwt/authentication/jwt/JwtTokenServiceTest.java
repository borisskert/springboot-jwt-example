package de.borisskert.springjwt.authentication.jwt;

import com.auth0.jwt.JWT;
import de.borisskert.springjwt.authentication.SecurityProperties;
import de.borisskert.springjwt.user.FakeUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class JwtTokenServiceTest {

    private static final String MY_ISSUER = "my issuer";
    private static final String MY_AUDIENCE = "my audience";
    private static final String MY_SECRET = "my secret";

    private JwtTokenService service;
    private SecurityProperties properties;

    @BeforeEach
    public void setup() throws Exception {
        properties = new SecurityProperties();
        properties.setIssuer(MY_ISSUER);
        properties.setAudience(MY_AUDIENCE);
        properties.setSecret(MY_SECRET);
        properties.setExpiration(1000L);

        service = new JwtTokenService(properties, Clock.systemUTC());
    }

    @Test
    public void shouldAuthenticateSignedToken() throws Exception {
        UserDetails userDetails = FakeUserDetails.of("admin", Set.of("ADMIN", "USER"));
        String jwt = service.createSignedTokenFor(userDetails);

        Optional<Authentication> maybeAuthenticated = service.tryToAuthenticate(jwt);

        assertThat(maybeAuthenticated.isPresent(), is(equalTo(true)));

        Authentication authentication = maybeAuthenticated.get();

        assertThat(authentication.getAuthorities(), containsInAnyOrder(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        ));
        assertThat(authentication.getPrincipal(), is(equalTo("admin")));
    }

    @Nested
    class NotAuthenticate {

        private JwtTokenService anotherService;

        @Nested
        class BadSecret {
            @BeforeEach
            public void setup() throws Exception {
                SecurityProperties properties = new SecurityProperties();
                properties.setIssuer(MY_ISSUER);
                properties.setAudience(MY_AUDIENCE);
                properties.setSecret("my other secret");
                properties.setExpiration(1000L);

                anotherService = new JwtTokenService(properties, Clock.systemUTC());
            }

            @Test
            public void shouldAuthenticateSignedToken() throws Exception {
                UserDetails userDetails = FakeUserDetails.of("admin", Set.of("ADMIN", "USER"));
                String jwt = service.createSignedTokenFor(userDetails);

                Optional<Authentication> maybe = anotherService.tryToAuthenticate(jwt);

                assertThat(maybe.isPresent(), is(equalTo(false)));
            }
        }

        @Nested
        class BadIssuer {
            @BeforeEach
            public void setup() throws Exception {
                SecurityProperties properties = new SecurityProperties();
                properties.setIssuer("my other issuer");
                properties.setAudience(MY_AUDIENCE);
                properties.setSecret(MY_SECRET);
                properties.setExpiration(1000L);

                anotherService = new JwtTokenService(properties, Clock.systemUTC());
            }

            @Test
            public void shouldAuthenticateSignedToken() throws Exception {
                UserDetails userDetails = FakeUserDetails.of("admin", Set.of("ADMIN", "USER"));
                String jwt = service.createSignedTokenFor(userDetails);

                Optional<Authentication> maybe = anotherService.tryToAuthenticate(jwt);

                assertThat(maybe.isPresent(), is(equalTo(false)));
            }
        }

        @Nested
        class BadAudience {
            @BeforeEach
            public void setup() throws Exception {
                SecurityProperties properties = new SecurityProperties();
                properties.setIssuer(MY_ISSUER);
                properties.setAudience("my other audience");
                properties.setSecret(MY_SECRET);
                properties.setExpiration(1000L);

                anotherService = new JwtTokenService(properties, Clock.systemUTC());
            }

            @Test
            public void shouldAuthenticateSignedToken() throws Exception {
                UserDetails userDetails = FakeUserDetails.of("admin", Set.of("ADMIN", "USER"));
                String jwt = service.createSignedTokenFor(userDetails);

                Optional<Authentication> maybe = anotherService.tryToAuthenticate(jwt);

                assertThat(maybe.isPresent(), is(equalTo(false)));
            }
        }
    }

    @Nested
    class WithinFixedClock {
        private Instant fixedTime;

        @BeforeEach
        public void setup() throws Exception {
            fixedTime = Instant.parse("2020-04-09T20:54:43.000Z"); // equals 1586465683L
            Clock fixedClock = Clock.fixed(fixedTime, ZoneOffset.UTC.normalized());

            service = new JwtTokenService(properties, fixedClock);
        }

        @Test
        public void shouldHaveIssuedAtDate() throws Exception {
            UserDetails userDetails = FakeUserDetails.of("admin", Set.of("ADMIN", "USER"));
            String jwt = service.createSignedTokenFor(userDetails);

            long issuedAt = JWT.decode(jwt).getClaim("iat").asLong();

            assertThat(issuedAt, is(equalTo(1586465683L)));
        }

        @Test
        public void shouldHaveExpireDate() throws Exception {
            UserDetails userDetails = FakeUserDetails.of("admin", Set.of("ADMIN", "USER"));
            String jwt = service.createSignedTokenFor(userDetails);

            long issuedAt = JWT.decode(jwt).getClaim("exp").asLong();

            assertThat(issuedAt, is(equalTo(1586465684L)));
        }

        /**
         * The expiration check is just second-precise
         */
        @Nested
        class WhenNotYetExpired {
            private JwtTokenService laterService;

            @BeforeEach
            public void setup() throws Exception {
                Instant notYetExpired = Instant.parse("2020-04-09T20:54:44.000Z");
                laterService = new JwtTokenService(properties, Clock.fixed(notYetExpired, ZoneOffset.UTC.normalized()));
            }

            @Test
            public void shouldNotAuthenticate() throws Exception {
                UserDetails userDetails = FakeUserDetails.of("admin", Set.of("ADMIN", "USER"));
                String jwt = service.createSignedTokenFor(userDetails);

                Optional<Authentication> maybe = laterService.tryToAuthenticate(jwt);

                assertThat(maybe.isPresent(), is(equalTo(true)));
            }
        }

        @Nested
        class WhenExpired {
            private JwtTokenService laterService;

            @BeforeEach
            public void setup() throws Exception {
                Instant expired = Instant.parse("2020-04-09T20:54:45.000Z");

                laterService = new JwtTokenService(properties, Clock.fixed(expired, ZoneOffset.UTC.normalized()));
            }

            @Test
            public void shouldNotAuthenticate() throws Exception {
                UserDetails userDetails = FakeUserDetails.of("admin", Set.of("ADMIN", "USER"));
                String jwt = service.createSignedTokenFor(userDetails);

                Optional<Authentication> maybe = laterService.tryToAuthenticate(jwt);

                assertThat(maybe.isPresent(), is(equalTo(false)));
            }
        }
    }
}
