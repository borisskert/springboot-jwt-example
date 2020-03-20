package de.borisskert.features.world;

import de.borisskert.features.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;
import static org.springframework.http.HttpStatus.*;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class UsersClient {

    private final CucumberHttpClient httpClient;

    private String latestLocation;

    @Autowired
    public UsersClient(CucumberHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void get(String userId) {
        httpClient.get("/api/users/" + userId);
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
        httpClient.post("/api/users/", user);
    }

    public void insert(String id, User user) {
        httpClient.put("/api/users/" + id, user);
    }

    public void userHasBeenCreated() {
        httpClient.verifyLatestStatus(CREATED);
        latestLocation = httpClient.getLatestResponseHeaderParam("Location");
    }

    public void userCreationWasConflicted() {
        httpClient.verifyLatestStatus(CONFLICT);
    }
}
