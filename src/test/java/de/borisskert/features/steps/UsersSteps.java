package de.borisskert.features.steps;

import de.borisskert.features.model.User;
import de.borisskert.features.model.UserWithId;
import de.borisskert.features.world.UsersClient;
import io.cucumber.java.Before;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


public class UsersSteps {

    @Autowired
    private UsersClient usersClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @When("I ask for a user with Id {string}")
    public void iAskForAUserWithId(String userId) {
        usersClient.get(userId);
    }

    @Then("I should return no users")
    public void shouldReturnNoUsers() {
        usersClient.userHasNotBeenFound();
    }

    @Given("A user has been created")
    public void aUserHasBeenCreatedWithId(User dataTable) {
        usersClient.create(dataTable);
    }

    @Then("I should get following user")
    public void iShouldGetFollowingUser(User dataTable) {
        usersClient.userHasBeenRetrieved(dataTable);
    }

    @And("A user exists with ID")
    public void aUserHasBeenInsertedWithID(UserWithId dataTable) {
        usersClient.insert(dataTable.id, dataTable.user);
    }

    @When("I create a user")
    public void iCreateAUser(User dataTable) {
        usersClient.create(dataTable);
    }

    @When("I get the user location after creation")
    public void iGetTheUserLocationAfterCreation() {
        usersClient.userHasBeenCreated();
    }

    @And("I ask for the user by location")
    public void iAskForTheUserByLocation() {
        usersClient.getUserByLocation();
    }

    @And("The location should get following user")
    public void theLocationShouldGetFollowingUser(User dataTable) {
        usersClient.userHasBeenRetrieved(dataTable);
    }

    @Then("I get a Conflict response")
    public void iGetAConflictResponse() {
        usersClient.userCreationWasConflicted();
    }

    @When("I insert a user with ID")
    public void iInsertAUserWithID(UserWithId dataTable) {
        usersClient.insert(dataTable.id, dataTable.user);
    }

    @DataTableType
    public User defineUser(Map<String, String> entry) {
        return User.from(entry);
    }

    @DataTableType
    public UserWithId defineUserWithId(Map<String, String> entry) {
        return UserWithId.from(entry);
    }
}
