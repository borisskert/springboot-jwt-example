package de.borisskert.springjwt.authentication;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@ConfigurationProperties(prefix = "app.security")
@Validated
public class SecurityProperties {

    @NotEmpty
    @Size(min = 8)
    private String secret;

    @Min(1)
    private Long expiration = 10 * 24 * 60 * 60 * 1000L;

    @NotEmpty
    @URL
    private String issuer;

    @NotEmpty
    private String audience;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }
}
