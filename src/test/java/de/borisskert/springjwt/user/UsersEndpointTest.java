package de.borisskert.springjwt.user;

import de.borisskert.springjwt.authentication.JwtTokenService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.borisskert.springjwt.user.MockUsers.NOT_EXISTING_ID;
import static de.borisskert.springjwt.user.MockUsers.USER_ID_TO_INSERT;
import static de.borisskert.springjwt.user.MockUsers.USER_ONE;
import static de.borisskert.springjwt.user.MockUsers.USER_ONE_ID;
import static de.borisskert.springjwt.user.MockUsers.USER_TO_CREATE;
import static de.borisskert.springjwt.user.MockUsers.USER_TO_INSERT;
import static de.borisskert.springjwt.user.MockUsers.USER_TO_SIGN_UP_AS_MAP;
import static de.borisskert.springjwt.user.MockUsers.USER_WITH_DUPLICATE_USERNAME;
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
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class UsersEndpointTest {
    private static final ParameterizedTypeReference<List<User>> USER_LIST_TYPE = new ParameterizedTypeReference<>() {
    };

    private static final String CREATED_USER_ID = "777";
    private static final String SIGN_UP_USER_ID = "555";
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
        when(userService.getUserById(USER_ONE_ID)).thenReturn(Optional.of(USER_ONE));
        when(userService.findByUsername("my_username")).thenReturn(Optional.of(USER_ONE));

        when(userService.getUserById(NOT_EXISTING_ID)).thenReturn(Optional.empty());

        when(userService.create(USER_TO_CREATE)).thenReturn(CREATED_USER_ID);

        when(userService.create(eq(USER_WITH_DUPLICATE_USERNAME))).thenThrow(new UserAlreadyExistsException("Username 'duplicate' already exists"));
        doThrow(new UserAlreadyExistsException("Username 'duplicate' already exists"))
                .when(userService).insert(any(), eq(USER_WITH_DUPLICATE_USERNAME));

        UsernamePasswordAuthenticationToken adminAuthentication = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(jwtTokenService.authenticate(ADMIN_TOKEN_VALUE)).thenReturn(adminAuthentication);

        UsernamePasswordAuthenticationToken userAuthentication = new UsernamePasswordAuthenticationToken(
                "user",
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(jwtTokenService.authenticate(USER_TOKEN_VALUE)).thenReturn(userAuthentication);
    }

    @Nested
    class GetById {
        @Test
        public void shouldRetrieveUserById() throws Exception {
            ResponseEntity<User> response = getUserByIdWithAdminRights(USER_ONE_ID);

            assertThat(response.getStatusCode(), is(equalTo(OK)));
            assertThat(response.getBody(), is(equalTo(USER_ONE)));
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
        @Test
        public void shouldFindUserByUsername() throws Exception {
            ResponseEntity<User> response = getUserByUsernameWithAdminRights("my_username");

            assertThat(response.getStatusCode(), is(equalTo(OK)));
            assertThat(response.getBody(), is(equalTo(USER_ONE)));
        }

        @Test
        public void shouldNotFindUserByUnknownUsername() throws Exception {
            ResponseEntity<Void> response = tryToGetUserByUsernameWithAdminRights("h4xx0r");

            assertThat(response.getStatusCode(), is(equalTo(NOT_FOUND)));
        }

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

        @Test
        public void shouldNotAcceptUserPermissions() throws Exception {
            ResponseEntity<Void> response = tryToGetUserByUsernameWithUserRights("my_username");

            assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
        }

        @Test
        public void shouldNotAcceptUnauthorized() throws Exception {
            ResponseEntity<Void> response = restTemplate.exchange(
                    API_USERS_URL + "?username=my_username",
                    HttpMethod.GET,
                    null,
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
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
        @Test
        public void shouldCreateUser() throws Exception {
            ResponseEntity<Void> response = createUserWithAdminRights(USER_TO_CREATE);

            assertThat(response.getStatusCode(), is(equalTo(CREATED)));
            assertThat(response.getHeaders().get("Location").get(0), is(equalTo("/api/users/777")));
        }

        @Test
        public void shouldNotAllowUserWithoutUsername() throws Exception {
            User userToCreate = User.from(null, "my@fakemail.com", LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithTooShortUsername() throws Exception {
            User userToCreate = User.from("short", "my@fakemail.com", LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithTooLongUsername() throws Exception {
            User userToCreate = User.from("tooooooo_long", "my@fakemail.com", LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutEmail() throws Exception {
            User userToCreate = User.from("my_username", null, LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithIllegalEmail() throws Exception {
            User userToCreate = User.from("my_username", "not_a_email", LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutBirthDate() throws Exception {
            User userToCreate = User.from("my_username", "my@fakemail.com", null);

            ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithIllegalBirthDate() throws Exception {
            User userToCreate = User.from("my_username", "my@fakemail.com", LocalDate.now().plusYears(1));

            ResponseEntity<Void> response = createUserWithAdminRights(userToCreate);
            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithDuplicateUsername() throws Exception {
            ResponseEntity<Void> response = createUserWithAdminRights(USER_WITH_DUPLICATE_USERNAME);
            assertThat(response.getStatusCode(), is(equalTo(CONFLICT)));
        }

        @Test
        public void shouldNotAllowRequestWithUserPermissions() throws Exception {
            ResponseEntity<Void> response = createUserWithUserRights(USER_TO_INSERT);
            assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
        }

        @Test
        public void shouldNotAllowRequestWithoutAuthentication() throws Exception {
            ResponseEntity<Void> response = restTemplate.exchange(
                    API_USERS_URL + "/" + USER_ID_TO_INSERT,
                    HttpMethod.PUT,
                    new HttpEntity<>(USER_TO_INSERT),
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
        }

        private ResponseEntity<Void> createUserWithAdminRights(User user) {
            return requestWithAdminRights(API_USERS_URL, HttpMethod.POST, user, Void.class);
        }

        private ResponseEntity<Void> createUserWithUserRights(User user) {
            return requestWithUserRights(API_USERS_URL, HttpMethod.POST, user, Void.class);
        }
    }

    @Nested
    class Put {
        @Test
        public void shouldInsertUser() throws Exception {
            ResponseEntity<Void> response = insertUserWithAdminRights(USER_ID_TO_INSERT, USER_TO_INSERT);

            assertThat(response.getStatusCode(), is(equalTo(OK)));
        }

        @Test
        public void shouldNotAllowToInsertUserWithIllegalId() throws Exception {
            ResponseEntity<Void> response = insertUserWithAdminRights("444", USER_TO_INSERT);

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutUsername() throws Exception {
            User userToCreate = User.from(null, "my@fakemail.com", LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = insertUserWithAdminRights(USER_ID_TO_INSERT, userToCreate);

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutEmail() throws Exception {
            User userToCreate = User.from("my_username", null, LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = insertUserWithAdminRights(USER_ID_TO_INSERT, userToCreate);

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithIllegalEmail() throws Exception {
            User userToCreate = User.from("my_username", "not_a_email", LocalDate.of(1944, 7, 20));

            ResponseEntity<Void> response = insertUserWithAdminRights(USER_ID_TO_INSERT, userToCreate);

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithoutBirthDate() throws Exception {
            User userToCreate = User.from("my_username", "my@fakemail.com", null);

            ResponseEntity<Void> response = insertUserWithAdminRights(USER_ID_TO_INSERT, userToCreate);

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithIllegalBirthDate() throws Exception {
            User userToCreate = User.from("my_username", "my@fakemail.com", LocalDate.now().plusYears(1));

            ResponseEntity<Void> response = insertUserWithAdminRights(USER_ID_TO_INSERT, userToCreate);

            assertThat(response.getStatusCode(), is(equalTo(BAD_REQUEST)));
        }

        @Test
        public void shouldNotAllowUserWithDuplicateUsername() throws Exception {
            ResponseEntity<Void> response = insertUserWithAdminRights(USER_ID_TO_INSERT, USER_WITH_DUPLICATE_USERNAME);
            assertThat(response.getStatusCode(), is(equalTo(CONFLICT)));
        }

        @Test
        public void shouldNotAllowRequestWithUserPermissions() throws Exception {
            ResponseEntity<Void> response = insertUserWithUserRights(USER_ID_TO_INSERT, USER_TO_INSERT);
            assertThat(response.getStatusCode(), is(equalTo(FORBIDDEN)));
        }

        @Test
        public void shouldNotAllowRequestWithoutAuthentication() throws Exception {
            ResponseEntity<Void> response = restTemplate.exchange(
                    API_USERS_URL + "/" + USER_ID_TO_INSERT,
                    HttpMethod.PUT,
                    new HttpEntity<>(USER_TO_INSERT),
                    Void.class
            );

            assertThat(response.getStatusCode(), is(equalTo(UNAUTHORIZED)));
        }

        private ResponseEntity<Void> insertUserWithAdminRights(String id, User user) {
            return requestWithAdminRights(API_USERS_URL + "/" + id, HttpMethod.PUT, user, Void.class);
        }

        private ResponseEntity<Void> insertUserWithUserRights(String id, User user) {
            return requestWithUserRights(API_USERS_URL + "/" + id, HttpMethod.PUT, user, Void.class);
        }
    }

    @Nested
    class SignUp {
        private static final String API_USERS_SIGN_UP_URL = "/api/users/sign-up";

        @Nested
        class Successful {
            @BeforeEach
            public void setup() throws Exception {
                reset(userService);
                when(userService.signUp(any())).thenReturn(SIGN_UP_USER_ID);
            }

            @Test
            public void shouldSignUpNewUser() throws Exception {
                ResponseEntity<Void> response = restTemplate.postForEntity(API_USERS_SIGN_UP_URL, USER_TO_SIGN_UP_AS_MAP, Void.class);

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