package de.borisskert.features.world;

import de.borisskert.features.model.User;
import de.borisskert.features.model.UserWithId;
import de.borisskert.features.model.UserWithPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class UsersClient {

    private static final String API_USERS_URL = "/api/users";

    private final CucumberHttpClient httpClient;
    private final AuthenticationClient authenticationClient;

    private String latestLocation;

    @Autowired
    public UsersClient(CucumberHttpClient httpClient, AuthenticationClient authenticationClient) {
        this.httpClient = httpClient;
        this.authenticationClient = authenticationClient;
    }

    public void get(String userId) {
        authenticationClient.getAuthorization()
                .ifPresent(authentication -> httpClient.addHeader(AuthenticationClient.AUTHORIZATION_HEADER, authentication));

        httpClient.get(API_USERS_URL + "/" + userId);
    }

    public void getUserByLocation() {
        authenticationClient.getAuthorization()
                .ifPresent(authentication -> httpClient.addHeader(AuthenticationClient.AUTHORIZATION_HEADER, authentication));

        httpClient.get(latestLocation);
    }

    public void userHasBeenRetrieved(User expectedUser) {
        httpClient.verifyLatestStatus(OK);
        httpClient.verifyLatestBodyIsEqualTo(expectedUser, User.class);
    }

    public void userHasNotBeenFound() {
        httpClient.verifyLatestStatus(NOT_FOUND);
    }

    public void create(User user) {
        authenticationClient.getAuthorization()
                .ifPresent(authentication -> httpClient.addHeader(AuthenticationClient.AUTHORIZATION_HEADER, authentication));

        httpClient.post(API_USERS_URL, user);
    }

    public final void insert(UserWithId user) {
        authenticationClient.getAuthorization()
                .ifPresent(authentication -> httpClient.addHeader(AuthenticationClient.AUTHORIZATION_HEADER, authentication));

        httpClient.put(API_USERS_URL + "/" + user.id, user);
    }

    public void userHasBeenCreated() {
        httpClient.verifyLatestStatus(CREATED);
        latestLocation = httpClient.getLatestResponseHeaderParam("Location");
    }

    public void userCreationWasConflicted() {
        httpClient.verifyLatestStatus(CONFLICT);
    }

    public void insert(List<UserWithId> usersWithId) {
        usersWithId.forEach(this::insert);
    }

    public void getAll() {
        authenticationClient.getAuthorization()
                .ifPresent(authentication -> httpClient.addHeader(AuthenticationClient.AUTHORIZATION_HEADER, authentication));

        httpClient.get(API_USERS_URL);
    }

    public void usersHasBeenRetrieved(List<User> dataTable) {
        authenticationClient.getAuthorization()
                .ifPresent(authentication -> httpClient.addHeader(AuthenticationClient.AUTHORIZATION_HEADER, authentication));

        httpClient.get(API_USERS_URL);
        httpClient.verifyLatestStatus(OK);
        httpClient.verifyLatestBodyContainsInAnyOrder(dataTable, User.LIST_TYPE);
    }

    public void signUp(UserWithPassword user) {
        httpClient.post(API_USERS_URL + "/sign-up", user);
    }

    public void getMe() {
        authenticationClient.getAuthorization()
                .ifPresent(authentication -> httpClient.addHeader(AuthenticationClient.AUTHORIZATION_HEADER, authentication));

        httpClient.get(API_USERS_URL + "/me");
    }
}
