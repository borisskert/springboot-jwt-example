package de.borisskert.features.steps;

import de.borisskert.features.FeaturesConfiguration;
import de.borisskert.springjpaliquibase.Application;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("IT")
@ContextConfiguration(
        classes = {
                Application.class,
                FeaturesConfiguration.class
        }
)
public class SpringBootSteps {

    @Autowired
    private ApplicationContext context;

    /**
     * This step is mandatory! Without this step cucumber will NOT load the Spring Boot context defined for this class.
     */
    @Given("our spring application is running")
    public void ourSpringApplicationIsRunning() {
        assertThat(context, is(not(nullValue())));
    }
}
