package de.borisskert.features.world;

import de.borisskert.features.model.Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class AuthenticationClient {
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final CucumberHttpClient httpClient;

    private String authorization;

    @Autowired
    public AuthenticationClient(CucumberHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void login(Credentials credentials) {
        httpClient.post("/login", credentials);
        authorization = httpClient.getLatestResponseHeaderParam(AUTHORIZATION_HEADER);
    }

    public Optional<String> getAuthorization() {
        return Optional.ofNullable(authorization);
    }
}
