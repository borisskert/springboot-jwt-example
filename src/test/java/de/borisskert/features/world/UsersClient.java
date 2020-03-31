package de.borisskert.features.world;

import de.borisskert.features.model.User;
import de.borisskert.features.model.UserWithId;
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

    private String latestLocation;

    @Autowired
    public UsersClient(CucumberHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void get(String userId) {
        httpClient.get(API_USERS_URL + "/" + userId);
    }

    public void getUserByLocation() {
        httpClient.get(latestLocation);
    }

    public void userHasBeenRetrieved(User expectedUser) {
        httpClient.verifyLatestStatus(OK);
        httpClient.verifyLatestBody(expectedUser, User.class);
    }

    public void userHasNotBeenFound() {
        httpClient.verifyLatestStatus(NOT_FOUND);
    }

    public void create(User user) {
        httpClient.post(API_USERS_URL, user);
    }

    public final void insert(String id, User user) {
        httpClient.put(API_USERS_URL + "/" + id, user);
    }

    public void userHasBeenCreated() {
        httpClient.verifyLatestStatus(CREATED);
        latestLocation = httpClient.getLatestResponseHeaderParam("Location");
    }

    public void userCreationWasConflicted() {
        httpClient.verifyLatestStatus(CONFLICT);
    }

    public void insert(List<UserWithId> usersWithId) {
        usersWithId.forEach(userWithId -> this.insert(userWithId.id, userWithId.user));
    }

    public void getAll() {
        httpClient.get(API_USERS_URL);
    }

    public void usersHasBeenRetrieved(List<User> dataTable) {
        httpClient.get(API_USERS_URL);
        httpClient.verifyLatestStatus(OK);
        httpClient.verifyLatestBody(dataTable, User.LIST_TYPE);
    }
}
