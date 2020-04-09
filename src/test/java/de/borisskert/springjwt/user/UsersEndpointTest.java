package de.borisskert.springjwt.user;

import de.borisskert.springjwt.authentication.jwt.JwtTokenService;
import de.borisskert.springjwt.user.exception.UserAlreadyExistsException;
import de.borisskert.springjwt.user.exception.UserNotFoundException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.borisskert.springjwt.user.MockUsers.USER_ONE;
import static de.borisskert.springjwt.user.MockUsers.USER_ONE_ID;
import static de.borisskert.springjwt.user.MockUsers.VALID_PASSWORD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("IT")
class UsersEndpointTest {
    private static final ParameterizedTypeReference<List<User>> USER_LIST_TYPE = new ParameterizedTypeReference<>() {
    };

    private static final String API_USERS_URL = "/api/users";
    private static final String ADMIN_TOKEN_VALUE = "MY_ADMIN_TOKEN_VALUE";
    private static final String USER_TOKEN_VALUE = "MY_USER_TOKEN_VALUE";

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private UserService userService;

    @MockBean
    private MeService meService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @BeforeEach
    public void setup() throws Exception {
        UsernamePasswordAuthenticationToken adminAuthentication = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(jwtTokenService.tryToAuthenticate(ADMIN_TOKEN_VALUE)).thenReturn(Optional.of(adminAuthentication));

        UsernamePasswordAuthenticationToken userAuthentication = new UsernamePasswordAuthenticationToken(
                "user",
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(jwtTokenService.tryToAuthenticate(USER_TOKEN_VALUE)).thenReturn(Optional.of(userAuthentication));
    }

    @Nested
    class GetById {

        @Nested
        class Found {
            @BeforeEach
            public void setup() throws Exception {
                reset(userService);
                when(userService.getUserById(USER_ONE_ID)).thenReturn(Optional.of(USER_ONE));
            }

            @Test
            public void shouldRetrieveUserById() throws Exception {
                ResponseEntity<User> response = getUserByIdWithAdminRights(USER_ONE_ID);

                assertThat(response.getStatusCode(), is(equalTo(OK)));
                assertThat(response.getBody(), is(equalTo(USER_ONE)));
            }

            @Test
            public void shouldNotAllowFindUserByIdWithUserRights() throws Exception {
                ResponseEntity<Void> response = tryToGetUserByIdWithUserRights(USER_ONE_ID);

                assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
            }

            @Test
            public void shouldNotAllowFindUserByIdWithoutRights() throws Exception {
                ResponseEntity<Void> response = restTemplate.exchange(
                        API_USERS_URL + "/" + USER_ONE_ID,
                        HttpMethod.GET,
                        null,
                        Void.class
                );

                assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
            }
        }

        @Nested
        class NotFound {
            private static final String NOT_EXISTING_ID = "9b686071-2973-4001-b0f9-6267422d45f7";

            @BeforeEach
            public void setup() throws Exception {
                reset(userService);
                when(userService.getUserById(NOT_EXISTING_ID)).thenReturn(Optional.empty());
            }

            @Test
            public void shouldNotFindWithUnknownId() throws Exception {
                ResponseEntity<Void> response = tryToGetUserByIdWithAdminRights(NOT_EXISTING_ID);

                assertThat(response.getStatusCode(), is(equalTo(NOT_FOUND)));
            }

            @Test
            public void shouldNotAllowFindUserByIdWithUserRights() throws Exception {
                ResponseEntity<Void> response = tryToGetUserByIdWithUserRights(USER_ONE_ID);

                assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
            }

            @Test
            public void shouldNotAllowFindUserByIdWithoutRights() throws Exception {
                ResponseEntity<Void> response = restTemplate.exchange(
                        API_USERS_URL + "/" + USER_ONE_ID,
                        HttpMethod.GET,
                        null,
                        Void.class
                );

                assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
            }
        }

        private ResponseEntity<User> getUserByIdWithAdminRights(String id) {
            return requestWithAdminRights(API_USERS_URL + "/" + id, HttpMethod.GET, null, User.class);
        }

        private ResponseEntity<Void> tryToGetUserByIdWithAdminRights(String id) {
            return requestWithAdminRights(API_USERS_URL + "/" + id, HttpMethod.GET, null, Void.class);
        }

        private ResponseEntity<Void> tryToGetUserByIdWithUserRights(String id) {
            return requestWithUserRights(API_USERS_URL + "/" + id, HttpMethod.GET, null, Void.class);
        }
    }

    @Nested
    class GetByUsername {
        private static final String USERNAME = "my_username";

        @Nested
        class Found {
            @BeforeEach
            public void setup() throws Exception {
                reset(userService);
                when(userService.findByUsername(USERNAME)).thenReturn(Optional.of(USER_ONE));
            }

            @Test
            public void shouldFindUserByUsername() throws Exception {
                ResponseEntity<User> response = getUserByUsernameWithAdminRights(USERNAME);

                assertThat(response.getStatusCode(), is(equalTo(OK)));
                assertThat(response.getBody(), is(equalTo(USER_ONE)));
            }

            @Test
            public void shouldNotAcceptUserPermissions() throws Exception {
                ResponseEntity<Void> response = tryToGetUserByUsernameWithUserRights(USERNAME);

                assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
            }

            @Test
            public void shouldNotAcceptUnauthorized() throws Exception {
                ResponseEntity<Void> response = restTemplate.exchange(
                        API_USERS_URL + "?username=" + USERNAME,
                        HttpMethod.GET,
                        null,
                        Void.class
                );

                assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
            }
        }

        @Nested
        class NotFound {
            @BeforeEach
            public void setup() throws Exception {
                reset(userService);
                when(userService.findByUsername(USERNAME)).thenReturn(Optional.empty());
            }

            @Test
            public void shouldNotFindUserByUnknownUsername() throws Exception {
                ResponseEntity<Void> response = tryToGetUserByUsernameWithAdminRights("h4xx0r");

                assertThat(response.getStatusCode(), is(equalTo(NOT_FOUND)));
            }

            @Test
            public void shouldNotAcceptUserPermissions() throws Exception {
                ResponseEntity<Void> response = tryToGetUserByUsernameWithUserRights(USERNAME);

                assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
            }

            @Test
            public void shouldNotAcceptUnauthorized() throws Exception {
                ResponseEntity<Void> response = restTemplate.exchange(
                        API_USERS_URL + "?username=" + USERNAME,
                        HttpMethod.GET,
                        null,
                        Void.class
                );

                assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
            }
        }

        @Nested
        class InvalidUsername {

            @Test
            public void shouldNotAcceptTooShortUsername() throws Exception {
                ResponseEntity<Void> response = tryToGetUserByUsernameWithAdminRights("ccc");

                assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
            }

            @Test
            public void shouldNotAcceptTooLongUsername() throws Exception {
                ResponseEntity<Void> response = tryToGetUserByUsernameWithAdminRights("mycrazyusernamewhichistolong");

                assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
            }
        }

        private ResponseEntity<User> getUserByUsernameWithAdminRights(String username) {
            return requestWithAdminRights(API_USERS_URL + "?username=" + username, HttpMethod.GET, null, User.class);
        }

        private ResponseEntity<Void> tryToGetUserByUsernameWithAdminRights(String username) {
            return requestWithAdminRights(API_USERS_URL + "?username=" + username, HttpMethod.GET, null, Void.class);
        }

        private ResponseEntity<Void> tryToGetUserByUsernameWithUserRights(String username) {
            return requestWithUserRights(API_USERS_URL + "?username=" + username, HttpMethod.GET, null, Void.class);
        }
    }

    @Nested
    class GetAll {

        private User userOne;
        private User userTwo;

        @BeforeEach
        public void setup() throws Exception {
            userOne = User.from("username 1", "user1@fakemail.com", LocalDate.of(1990, 10, 3));
            userTwo = User.from("username 2", "user2@fakemail.com", LocalDate.of(1990, 10, 2));

            when(userService.getAllUsers()).thenReturn(List.of(userOne, userTwo));
        }

        @Test
        public void shouldAllowWithAdminRights() throws Exception {
            ResponseEntity<List<User>> response = getAllUsersWithAdminRights();

            assertThat(response.getStatusCode(), is(equalTo(OK)));

            List<User> body = response.getBody();
            assertThat(body, containsInAnyOrder(userOne, userTwo));
        }

        @Test
        public void shouldNotAllowRequestWithoutAuthentication() throws Exception {
            ResponseEntity<Void> response = restTemplate.exchange(
                    API_USERS_URL,
                    HttpMethod.GET,
                    null,
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
        }

        @Test
        public void shouldNotAllowRequestWithUserRights() throws Exception {
            ResponseEntity<Void> response = requestWithUserRights(API_USERS_URL, HttpMethod.GET, null, Void.class);

            assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
        }

        private ResponseEntity<List<User>> getAllUsersWithAdminRights() {
            return requestWithAdminRights(API_USERS_URL, HttpMethod.GET, null, USER_LIST_TYPE);
        }
    }

    @Nested
    class GetMe {

        private User myUser;

        @BeforeEach
        public void setup() throws Exception {
            myUser = User.from(
                    "my username",
                    "my@fakemail.com",
                    LocalDate.of(1989, 11, 8),
                    List.of("USER", "ADMIN")
            );
            when(meService.getMe()).thenReturn(Optional.of(myUser));
        }

        @Test
        public void shouldAllowToGetMyUserWithAdminPermissions() throws Exception {
            ResponseEntity<User> response = getMyUserWithAdminPermissions();

            assertThat(response.getStatusCode(), is(equalTo(OK)));
            assertThat(response.getBody(), is(equalTo(myUser)));
        }

        @Test
        public void shouldAllowToGetMyUserWithUserPermissions() throws Exception {
            ResponseEntity<User> response = getMyUserWithUserPermissions();

            assertThat(response.getStatusCode(), is(equalTo(OK)));
            assertThat(response.getBody(), is(equalTo(myUser)));
        }

        @Test
        public void shouldNotAllowToGetMyUserWithoutPermissions() throws Exception {
            ResponseEntity<Void> response = restTemplate.getForEntity(API_USERS_URL + "/me", Void.class);

            assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
        }

        private ResponseEntity<User> getMyUserWithAdminPermissions() {
            return requestWithAdminRights(API_USERS_URL + "/me", HttpMethod.GET, null, User.class);
        }

        private ResponseEntity<Void> tryToGetMyUserWithAdminPermissions() {
            return requestWithAdminRights(API_USERS_URL + "/me", HttpMethod.GET, null, Void.class);
        }

        private ResponseEntity<User> getMyUserWithUserPermissions() {
            return requestWithUserRights(API_USERS_URL + "/me", HttpMethod.GET, null, User.class);
        }

        @Nested
        class NotFound {
            @BeforeEach
            public void setup() throws Exception {
                reset(meService);
                when(meService.getMe()).thenReturn(Optional.empty());
            }

            @Test
            public void shouldAllowToGetMyUserWithAdminPermissions() throws Exception {
                ResponseEntity<Void> response = tryToGetMyUserWithAdminPermissions();

                assertThat(response.getStatusCode(), is(equalTo(NOT_FOUND)));
            }

            @Test
            public void shouldAllowToGetMyUserWithUserPermissions() throws Exception {
                ResponseEntity<Void> response = tryToGetMyUserWithAdminPermissions();

                assertThat(response.getStatusCode(), is(equalTo(NOT_FOUND)));
            }

            @Test
            public void shouldNotAllowToGetMyUserWithoutPermissions() throws Exception {
                ResponseEntity<Void> response = restTemplate.getForEntity(API_USERS_URL + "/me", Void.class);

                assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
            }
        }
    }

    @Nested
    class Post {
        private Map<String, Object> userToCreate;

        @Nested
        class CorrectBody {
            private static final String CREATED_USER_ID = "31bde056-7e55-49c5-a8b2-04da6109bd16";

            @BeforeEach
            public void setup() throws Exception {
                reset(userService);

                userToCreate = Map.of(
                        "username", "my_username",
                        "email", "my@fakemail.com",
                        "dateOfBirth", "1945-05-08",
                        "roles", List.of("USER")
                );

                when(userService.create(any(User.class))).thenReturn(CREATED_USER_ID);
            }

            @Test
            public void shouldCreateUser() throws Exception {
                ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);

                assertThat(response.getStatusCode(), is(equalTo(CREATED)));
                assertThat(response.getHeaders().get("Location").get(0), is(equalTo("/api/users/31bde056-7e55-49c5-a8b2-04da6109bd16")));
            }

            @Test
            public void shouldNotAllowRequestWithUserPermissions() throws Exception {
                ResponseEntity<Void> response = createUserWithUserRights(userToCreate);
                assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
            }

            @Test
            public void shouldNotAllowRequestWithoutAuthentication() throws Exception {
                ResponseEntity<Void> response = restTemplate.exchange(
                        API_USERS_URL,
                        HttpMethod.POST,
                        new HttpEntity<>(userToCreate),
                        Void.class
                );

                assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
            }
        }

        @Nested
        class DuplicateUsername {
            @BeforeEach
            public void setup() throws Exception {
                reset(userService);

                userToCreate = Map.of(
                        "username", "my_username",
                        "email", "my@fakemail.com",
                        "dateOfBirth", "1945-05-08",
                        "roles", List.of("USER")
                );

                when(userService.create(any(User.class))).thenThrow(new UserAlreadyExistsException("Username 'my_username' already exists"));
            }

            @Test
            public void shouldNotAllowUserWithDuplicateUsername() throws Exception {
                ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
                assertThat(response.getStatusCode(), is(equalTo(CONFLICT)));
            }
        }

        @Nested
        class Invalid {
            @Nested
            class InvalidUsername {
                @Test
                public void shouldNotAllowUserWithoutUsername() throws Exception {
                    userToCreate = Map.of(
                            "email", "my@fakemail.com",
                            "dateOfBirth", "1945-05-08",
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }

                @Test
                public void shouldNotAllowUserWithTooShortUsername() throws Exception {
                    userToCreate = Map.of(
                            "username", "short",
                            "email", "my@fakemail.com",
                            "dateOfBirth", "1945-05-08",
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }

                @Test
                public void shouldNotAllowUserWithTooLongUsername() throws Exception {

                    userToCreate = Map.of(
                            "username", "tooooooo_long",
                            "email", "my@fakemail.com",
                            "dateOfBirth", "1945-05-08",
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }
            }

            @Nested
            class InvalidEmail {
                @Test
                public void shouldNotAllowUserWithoutEmail() throws Exception {
                    userToCreate = Map.of(
                            "username", "my_username",
                            "dateOfBirth", "1945-05-08",
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }

                @Test
                public void shouldNotAllowUserWithIllegalEmail() throws Exception {
                    userToCreate = Map.of(
                            "username", "my_username",
                            "email", "not a email",
                            "dateOfBirth", "1945-05-08",
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }
            }

            @Nested
            class InvalidDateOfBirth {
                @Test
                public void shouldNotAllowUserWithoutBirthDate() throws Exception {
                    userToCreate = Map.of(
                            "username", "my_username",
                            "email", "my@fakemail.com",
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }

                @Test
                public void shouldNotAllowUserWithIllegalBirthDate() throws Exception {
                    userToCreate = Map.of(
                            "username", "my_username",
                            "email", "my@fakemail.com",
                            "dateOfBirth", LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_DATE),
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }
            }
        }

        private ResponseEntity<Void> createUserWithAdminRights(Map<String, Object> user) {
            return requestWithAdminRights(API_USERS_URL, HttpMethod.POST, user, Void.class);
        }

        private ResponseEntity<Void> createUserWithUserRights(Map<String, Object> user) {
            return requestWithUserRights(API_USERS_URL, HttpMethod.POST, user, Void.class);
        }
    }

    @Nested
    class Put {
        private static final String VALID_USER_ID = "d67c5afa-96a4-4fea-a9ea-221448ea5b68";

        private Map<String, Object> userToInsert;

        @BeforeEach
        public void setup() throws Exception {
            userToInsert = Map.of(
                    "username", "my_username",
                    "email", "user@fakemail.com",
                    "dateOfBirth", "1948-06-21",
                    "roles", List.of("USER")
            );
        }

        @Nested
        class Successful {

            @BeforeEach
            public void setup() throws Exception {
                reset(userService);
            }

            @Test
            public void shouldInsertUser() throws Exception {
                ResponseEntity<Void> response = insertUserWithAdminRights(VALID_USER_ID, userToInsert);

                assertThat(response.getStatusCode(), is(equalTo(OK)));
            }
        }

        @Nested
        class IllegalUserId {
            @Test
            public void shouldNotAllowToInsertUserWithIllegalId() throws Exception {
                ResponseEntity<Void> response = insertUserWithAdminRights("444", userToInsert);

                assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
            }
        }

        @Nested
        class InvalidUser {
            private Map<String, Object> invalidUserToInsert;

            @Nested
            class InvalidUsername {
                @Test
                public void shouldNotAllowUserWithoutUsername() throws Exception {
                    invalidUserToInsert = Map.of(
                            "email", "user@fakemail.com",
                            "dateOfBirth", "1948-06-21",
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = insertUserWithAdminRights(VALID_USER_ID, invalidUserToInsert);

                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }
            }

            @Nested
            class InvalidEmail {
                @Test
                public void shouldNotAllowUserWithoutEmail() throws Exception {
                    invalidUserToInsert = Map.of(
                            "username", "my_username",
                            "dateOfBirth", "1948-06-21",
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = insertUserWithAdminRights(VALID_USER_ID, invalidUserToInsert);

                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }

                @Test
                public void shouldNotAllowUserWithIllegalEmail() throws Exception {
                    invalidUserToInsert = Map.of(
                            "username", "my_username",
                            "email", "not_a_email",
                            "dateOfBirth", "1948-06-21",
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = insertUserWithAdminRights(VALID_USER_ID, invalidUserToInsert);

                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }
            }

            @Nested
            class InvalidDateOfBirth {
                @Test
                public void shouldNotAllowUserWithoutBirthDate() throws Exception {
                    invalidUserToInsert = Map.of(
                            "username", "my_username",
                            "email", "not_a_email",
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = insertUserWithAdminRights(VALID_USER_ID, invalidUserToInsert);

                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }

                @Test
                public void shouldNotAllowUserWithIllegalBirthDate() throws Exception {
                    invalidUserToInsert = Map.of(
                            "username", "my_username",
                            "email", "not_a_email",
                            "dateOfBirth", "not a birth date",
                            "roles", List.of("USER")
                    );

                    ResponseEntity<Void> response = insertUserWithAdminRights(VALID_USER_ID, invalidUserToInsert);

                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }
            }
        }

        @Nested
        class DuplicateUsername {

            @BeforeEach
            public void setup() throws Exception {
                reset(userService);

                doThrow(new UserAlreadyExistsException("Username 'duplicate' already exists"))
                        .when(userService).insert(eq(VALID_USER_ID), any());
            }

            @Test
            public void shouldNotAllowUserWithDuplicateUsername() throws Exception {
                ResponseEntity<Void> response = insertUserWithAdminRights(VALID_USER_ID, userToInsert);
                assertThat(response.getStatusCode(), is(equalTo(CONFLICT)));
            }
        }

        @Test
        public void shouldNotAllowRequestWithUserPermissions() throws Exception {
            ResponseEntity<Void> response = insertUserWithUserRights(VALID_USER_ID, userToInsert);
            assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
        }

        @Test
        public void shouldNotAllowRequestWithoutAuthentication() throws Exception {
            ResponseEntity<Void> response = restTemplate.exchange(
                    API_USERS_URL + "/" + VALID_USER_ID,
                    HttpMethod.PUT,
                    new HttpEntity<>(userToInsert),
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
        }

        private ResponseEntity<Void> insertUserWithAdminRights(String id, Map<String, Object> user) {
            return requestWithAdminRights(API_USERS_URL + "/" + id, HttpMethod.PUT, user, Void.class);
        }

        private ResponseEntity<Void> insertUserWithUserRights(String id, Map<String, Object> user) {
            return requestWithUserRights(API_USERS_URL + "/" + id, HttpMethod.PUT, user, Void.class);
        }
    }

    @Nested
    class Patch {

        private RestTemplate patchRestTemplate;

        @BeforeEach
        public void setup() throws Exception {
            patchRestTemplate = createPatchRestTemplate();
        }

        @Nested
        class Successful {
            private static final String USER_ID = "2884a717-5a17-49fa-84cc-d4321207c7f9";

            @Nested
            class FullPatch {
                private Map<String, Object> patch;

                @BeforeEach
                public void setup() throws Exception {
                    reset(userService);

                    patch = Map.of(
                            "username", "my_username",
                            "email", "user@fakemail.com",
                            "dateOfBirth", "1990-10-03",
                            "roles", List.of("USER")
                    );
                }

                @Test
                public void shouldAllowPatchUserWithAdminPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithAdminRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(NO_CONTENT)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithUserPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithUserRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithoutPermissions() throws Exception {
                    ResponseEntity<Void> response = restTemplate.exchange(
                            API_USERS_URL + "/" + USER_ID,
                            HttpMethod.PATCH,
                            new HttpEntity<>(patch),
                            Void.class
                    );

                    assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
                }
            }

            @Nested
            class UsernameOnly {
                private Map<String, Object> patch;

                @BeforeEach
                public void setup() throws Exception {
                    patch = Map.of(
                            "username", "my_username",
                            "email", "user@fakemail.com",
                            "dateOfBirth", "1990-10-03",
                            "roles", List.of("USER")
                    );
                }

                @Test
                public void shouldAllowPatchUserWithAdminPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithAdminRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(NO_CONTENT)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithUserPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithUserRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithoutPermissions() throws Exception {
                    ResponseEntity<Void> response = restTemplate.exchange(
                            API_USERS_URL + "/" + USER_ID,
                            HttpMethod.PATCH,
                            new HttpEntity<>(patch),
                            Void.class
                    );

                    assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
                }
            }

            @Nested
            class EmailOnly {
                private Map<String, Object> patch;

                @BeforeEach
                public void setup() throws Exception {
                    patch = Map.of(
                            "username", "my_username",
                            "email", "user@fakemail.com",
                            "dateOfBirth", "1990-10-03",
                            "roles", List.of("USER")
                    );
                }

                @Test
                public void shouldAllowPatchUserWithAdminPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithAdminRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(NO_CONTENT)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithUserPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithUserRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithoutPermissions() throws Exception {
                    ResponseEntity<Void> response = restTemplate.exchange(
                            API_USERS_URL + "/" + USER_ID,
                            HttpMethod.PATCH,
                            new HttpEntity<>(patch),
                            Void.class
                    );

                    assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
                }
            }

            @Nested
            class DateOfBirthOnly {
                private Map<String, Object> patch;

                @BeforeEach
                public void setup() throws Exception {
                    patch = Map.of(
                            "username", "my_username",
                            "email", "user@fakemail.com",
                            "dateOfBirth", "1990-10-03",
                            "roles", List.of("USER")
                    );
                }

                @Test
                public void shouldAllowPatchUserWithAdminPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithAdminRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(NO_CONTENT)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithUserPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithUserRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithoutPermissions() throws Exception {
                    ResponseEntity<Void> response = restTemplate.exchange(
                            API_USERS_URL + "/" + USER_ID,
                            HttpMethod.PATCH,
                            new HttpEntity<>(patch),
                            Void.class
                    );

                    assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
                }
            }

            @Nested
            class RolesOnly {
                private Map<String, Object> patch;

                @BeforeEach
                public void setup() throws Exception {
                    reset(userService);

                    patch = Map.of(
                            "username", "my_username",
                            "email", "user@fakemail.com",
                            "dateOfBirth", "1990-10-03",
                            "roles", List.of("USER")
                    );
                }

                @Test
                public void shouldAllowPatchUserWithAdminPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithAdminRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(NO_CONTENT)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithUserPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithUserRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithoutPermissions() throws Exception {
                    ResponseEntity<Void> response = restTemplate.exchange(
                            API_USERS_URL + "/" + USER_ID,
                            HttpMethod.PATCH,
                            new HttpEntity<>(patch),
                            Void.class
                    );

                    assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
                }
            }
        }

        @Nested
        class UserNotFound {
            private static final String USER_ID = "2884a717-5a17-49fa-84cc-d4321207c7f9";
            private Map<String, Object> patch;

            @BeforeEach
            public void setup() throws Exception {
                reset(userService);

                patch = Map.of(
                        "username", "my_username",
                        "email", "user@fakemail.com",
                        "dateOfBirth", "1990-10-03",
                        "roles", List.of("USER")
                );

                doThrow(new UserNotFoundException("User with id '2884a717-5a17-49fa-84cc-d4321207c7f9' not found"))
                        .when(userService).patch(eq(USER_ID), any());
            }

            @Test
            public void shouldAllowPatchUserWithAdminPermissions() throws Exception {
                ResponseEntity<Void> response = patchUserWithAdminRights(USER_ID, patch);

                assertThat(response.getStatusCode(), is(equalTo(NOT_FOUND)));
            }

            @Test
            public void shouldNotAllowToPatchUserWithUserPermissions() throws Exception {
                ResponseEntity<Void> response = patchUserWithUserRights(USER_ID, patch);

                assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
            }

            @Test
            public void shouldNotAllowToPatchUserWithoutPermissions() throws Exception {
                ResponseEntity<Void> response = restTemplate.exchange(
                        API_USERS_URL + "/" + USER_ID,
                        HttpMethod.PATCH,
                        new HttpEntity<>(patch),
                        Void.class
                );

                assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
            }
        }

        @Nested
        class InvalidUserId {
            private static final String USER_ID = "abc";
            private Map<String, Object> patch;

            @BeforeEach
            public void setup() throws Exception {
                patch = Map.of(
                        "username", "my_username",
                        "email", "user@fakemail.com",
                        "dateOfBirth", "1990-10-03",
                        "roles", List.of("USER")
                );
            }

            @Test
            public void shouldNotAllowPatchUserByInvalidUserIdWithAdminPermissions() throws Exception {
                ResponseEntity<Void> response = patchUserWithAdminRights(USER_ID, patch);

                assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
            }

            @Test
            public void shouldNotAllowToPatchUserWithUserPermissions() throws Exception {
                ResponseEntity<Void> response = patchUserWithUserRights(USER_ID, patch);

                assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
            }

            @Test
            public void shouldNotAllowToPatchUserWithoutPermissions() throws Exception {
                ResponseEntity<Void> response = restTemplate.exchange(
                        API_USERS_URL + "/" + USER_ID,
                        HttpMethod.PATCH,
                        new HttpEntity<>(patch),
                        Void.class
                );

                assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
            }
        }

        @Nested
        class InvalidUserPatch {
            private static final String USER_ID = "2884a717-5a17-49fa-84cc-d4321207c7f9";

            @Nested
            class InvalidUsername {
                private Map<String, Object> patch;

                @BeforeEach
                public void setup() throws Exception {
                    patch = Map.of(
                            "username", "abc",
                            "email", "user@fakemail.com",
                            "dateOfBirth", "1990-10-03",
                            "roles", List.of("USER")
                    );
                }

                @Test
                public void shouldNotAllowPatchUserByInvalidUserIdWithAdminPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithAdminRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithUserPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithUserRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithoutPermissions() throws Exception {
                    ResponseEntity<Void> response = restTemplate.exchange(
                            API_USERS_URL + "/" + USER_ID,
                            HttpMethod.PATCH,
                            new HttpEntity<>(patch),
                            Void.class
                    );

                    assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
                }
            }

            @Nested
            class InvalidEmail {
                private Map<String, Object> patch;

                @BeforeEach
                public void setup() throws Exception {
                    patch = Map.of(
                            "username", "my_username",
                            "email", "not a mail",
                            "dateOfBirth", "1990-10-03",
                            "roles", List.of("USER")
                    );
                }

                @Test
                public void shouldNotAllowPatchUserByInvalidUserIdWithAdminPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithAdminRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithUserPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithUserRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithoutPermissions() throws Exception {
                    ResponseEntity<Void> response = restTemplate.exchange(
                            API_USERS_URL + "/" + USER_ID,
                            HttpMethod.PATCH,
                            new HttpEntity<>(patch),
                            Void.class
                    );

                    assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
                }
            }

            @Nested
            class InvalidDate {
                private Map<String, Object> patch;

                @BeforeEach
                public void setup() throws Exception {
                    patch = Map.of(
                            "username", "my_username",
                            "email", "user@fakemail.com",
                            "dateOfBirth", "not a date",
                            "roles", List.of("USER")
                    );
                }

                @Test
                public void shouldNotAllowPatchUserByInvalidUserIdWithAdminPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithAdminRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithUserPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithUserRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithoutPermissions() throws Exception {
                    ResponseEntity<Void> response = restTemplate.exchange(
                            API_USERS_URL + "/" + USER_ID,
                            HttpMethod.PATCH,
                            new HttpEntity<>(patch),
                            Void.class
                    );

                    assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
                }
            }

            @Nested
            class InvalidRoles {
                private Map<String, Object> patch;

                @BeforeEach
                public void setup() throws Exception {
                    patch = Map.of(
                            "username", "my_username",
                            "email", "user@fakemail.com",
                            "dateOfBirth", "not a date",
                            "roles", "not a list"
                    );
                }

                @Test
                public void shouldNotAllowPatchUserByInvalidUserIdWithAdminPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithAdminRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithUserPermissions() throws Exception {
                    ResponseEntity<Void> response = patchUserWithUserRights(USER_ID, patch);

                    assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
                }

                @Test
                public void shouldNotAllowToPatchUserWithoutPermissions() throws Exception {
                    ResponseEntity<Void> response = restTemplate.exchange(
                            API_USERS_URL + "/" + USER_ID,
                            HttpMethod.PATCH,
                            new HttpEntity<>(patch),
                            Void.class
                    );

                    assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
                }
            }
        }

        private ResponseEntity<Void> patchUserWithAdminRights(String userId, Map<String, Object> userPatch) {
            return requestWithAdminRights(API_USERS_URL + "/" + userId, HttpMethod.PATCH, userPatch, Void.class);
        }

        private ResponseEntity<Void> patchUserWithUserRights(String userId, Map<String, Object> userPatch) {
            return requestWithUserRights(API_USERS_URL + "/" + userId, HttpMethod.PATCH, userPatch, Void.class);
        }

        private <T> ResponseEntity<T> requestWithAdminRights(String url, HttpMethod method, Object body, Class<T> responseType) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + ADMIN_TOKEN_VALUE);

            HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);

            return restTemplate.exchange(url, method, httpEntity, responseType);
        }

        private <T> ResponseEntity<T> requestWithUserRights(String url, HttpMethod method, Object body, Class<T> responseType) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + USER_TOKEN_VALUE);

            HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);

            return patchRestTemplate.exchange(url, method, httpEntity, responseType);
        }

        private RestTemplate createPatchRestTemplate() {
            RestTemplate patchRestTemplate = restTemplate.getRestTemplate();

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

            patchRestTemplate.setRequestFactory(requestFactory);

            return patchRestTemplate;
        }
    }

    @Nested
    class SignUp {
        private static final String API_USERS_SIGN_UP_URL = "/api/users/sign-up";
        private static final String SIGN_UP_USER_ID = "555";

        private Map<String, Object> userToSignUp;

        @BeforeEach
        public void setup() throws Exception {
            userToSignUp = Map.of(
                    "username", "sign_up2",
                    "email", "user_to_sign_up@fakemail.com",
                    "dateOfBirth", "1943-11-29",
                    "rawPassword", VALID_PASSWORD
            );
        }

        @Nested
        class Successful {
            @BeforeEach
            public void setup() throws Exception {
                reset(userService);
                when(userService.signUp(any())).thenReturn(SIGN_UP_USER_ID);
            }

            @Test
            public void shouldSignUpNewUser() throws Exception {
                ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, userToSignUp, Void.class);

                assertThat(response.getStatusCode(), is(equalTo(CREATED)));
                assertThat(response.getHeaders().get("Location").get(0), is(equalTo("/api/users/" + SIGN_UP_USER_ID)));
            }
        }

        @Nested
        class Conflict {
            @BeforeEach
            public void setup() throws Exception {
                reset(userService);
                when(userService.signUp(any())).thenThrow(
                        new UserAlreadyExistsException("Username 'duplicate' already exists")
                );
            }

            @Test
            public void shouldNotAllowUserWithDuplicateUsername() throws Exception {
                Map<String, String> user = Map.of(
                        "username", "sign_up2",
                        "email", "user_to_sign_up@fakemail.com",
                        "dateOfBirth", "1943-11-29",
                        "rawPassword", VALID_PASSWORD
                );

                ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, user, Void.class);
                assertThat(response.getStatusCode(), is(equalTo(CONFLICT)));
            }
        }

        @Test
        public void shouldNotAllowToSignUpNewUserWithoutPassword() throws Exception {
            Map<String, String> withoutPassword = Map.of(
                    "username", "sign_up2",
                    "email", "user_to_sign_up@fakemail.com",
                    "dateOfBirth", "1943-11-29"
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withoutPassword, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowToSignUpNewUserWithTooShortPassword() throws Exception {
            Map<String, String> withShortPassword = Map.of(
                    "username", "sign_up2",
                    "email", "user_to_sign_up@fakemail.com",
                    "dateOfBirth", "1943-11-29",
                    "rawPassword", "@bc123"
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withShortPassword, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowToSignUpNewUserWithPasswordWithoutDigits() throws Exception {
            Map<String, String> withoutDigits = Map.of(
                    "username", "sign_up2",
                    "email", "user_to_sign_up@fakemail.com",
                    "dateOfBirth", "1943-11-29",
                    "rawPassword", "@bcdefgh"
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withoutDigits, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowToSignUpNewUserWithPasswordWithoutLetters() throws Exception {
            Map<String, String> withoutLetters = Map.of(
                    "username", "sign_up2",
                    "email", "user_to_sign_up@fakemail.com",
                    "dateOfBirth", "1943-11-29",
                    "rawPassword", "@2345678"
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withoutLetters, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowToSignUpNewUserWithPasswordWithoutSpecialCharacters() throws Exception {
            Map<String, String> withoutSpecialCharacters = Map.of(
                    "username", "sign_up2",
                    "email", "user_to_sign_up@fakemail.com",
                    "dateOfBirth", "1943-11-29",
                    "rawPassword", "a2345678"
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withoutSpecialCharacters, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutUsername() throws Exception {
            Map<String, String> withoutUsername = Map.of(
                    "email", "user_to_sign_up@fakemail.com",
                    "dateOfBirth", "1943-11-29",
                    "rawPassword", VALID_PASSWORD
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withoutUsername, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithTooShortUsername() throws Exception {
            Map<String, String> withShortUsername = Map.of(
                    "username", "short",
                    "email", "user_to_sign_up@fakemail.com",
                    "dateOfBirth", "1943-11-29",
                    "rawPassword", VALID_PASSWORD
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withShortUsername, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithTooLongUsername() throws Exception {
            Map<String, String> withLongUsername = Map.of(
                    "username", "tooooooo_long",
                    "email", "user_to_sign_up@fakemail.com",
                    "dateOfBirth", "1943-11-29",
                    "rawPassword", VALID_PASSWORD
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withLongUsername, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutEmail() throws Exception {
            Map<String, String> withoutEmail = Map.of(
                    "username", "my_username",
                    "dateOfBirth", "1943-11-29",
                    "rawPassword", VALID_PASSWORD
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withoutEmail, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithIllegalEmail() throws Exception {
            Map<String, String> withIllegalEmail = Map.of(
                    "username", "my_username",
                    "email", "not_a_email",
                    "dateOfBirth", "1943-11-29",
                    "rawPassword", VALID_PASSWORD
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withIllegalEmail, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutBirthDate() throws Exception {
            Map<String, String> withoutBirthDate = Map.of(
                    "username", "my_username",
                    "email", "my@fakemail.com",
                    "rawPassword", VALID_PASSWORD
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withoutBirthDate, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithIllegalBirthDate() throws Exception {
            Map<String, String> withoutBirthDate = Map.of(
                    "username", "my_username",
                    "email", "my@fakemail.com",
                    "dateOfBirth", LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_DATE),
                    "rawPassword", VALID_PASSWORD
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, withoutBirthDate, Void.class);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }
    }

    @AfterEach
    public void cleanup() {
        reset(userService, meService, jwtTokenService);
    }

    private <T> ResponseEntity<T> requestWithAdminRights(String url, HttpMethod method, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + ADMIN_TOKEN_VALUE);

        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, method, httpEntity, responseType);
    }

    private <T> ResponseEntity<List<T>> requestWithAdminRights(String url, HttpMethod method, Object body, ParameterizedTypeReference<List<T>> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + ADMIN_TOKEN_VALUE);

        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, method, httpEntity, responseType);
    }

    private <T> ResponseEntity<T> requestWithUserRights(String url, HttpMethod method, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + USER_TOKEN_VALUE);

        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, method, httpEntity, responseType);
    }
}
