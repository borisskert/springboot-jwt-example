package de.borisskert.features.steps;

import de.borisskert.features.model.Credentials;
import de.borisskert.features.world.AuthenticationClient;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthenticationSteps {

    @Autowired
    private AuthenticationClient authClient;

    @When("I log in as")
    public void iLogInAs(Credentials dataTable) {
        authClient.login(dataTable);
    }
}
