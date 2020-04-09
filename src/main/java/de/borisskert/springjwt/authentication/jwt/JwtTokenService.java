package de.borisskert.springjwt.authentication.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier.BaseVerification;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.borisskert.springjwt.authentication.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Service
public class JwtTokenService {
    private static final String ROLES_CLAIM_NAME = "roles";

    private final SecurityProperties properties;
    private final Clock clock;

    @Autowired
    public JwtTokenService(SecurityProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public String createSignedTokenFor(UserDetails credentials) {
        long issuedAt = clock.instant().toEpochMilli();
        long expiresAt = issuedAt + properties.getExpiration();

        Algorithm hmac512 = HMAC512(properties.getSecret().getBytes());

        List<String> roles = credentials.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableList());

        return JWT.create()
                .withSubject(credentials.getUsername())
                .withIssuer(properties.getIssuer())
                .withAudience(properties.getAudience())
                .withIssuedAt(new Date(issuedAt))
                .withClaim(ROLES_CLAIM_NAME, roles)
                .withExpiresAt(new Date(expiresAt))
                .sign(hmac512);
    }

    public Optional<Authentication> tryToAuthenticate(String tokenValue) {
        return tryToParseJwt(tokenValue)
                .map(this::mapToAuthentication);
    }

    private Authentication mapToAuthentication(DecodedJWT decodedJwt) {
        String user = decodedJwt.getSubject();

        List<String> roles = decodedJwt.getClaim(ROLES_CLAIM_NAME)
                .asList(String.class);

        return JwtAuthentication.of(user, roles);
    }

    private Optional<DecodedJWT> tryToParseJwt(String token) {
        byte[] secretInBytes = properties.getSecret().getBytes();
        Algorithm hmacSha512 = Algorithm.HMAC512(secretInBytes);

        try {
            BaseVerification verification = (BaseVerification) JWT.require(hmacSha512)
                    .withIssuer(properties.getIssuer())
                    .withAudience(properties.getAudience());

            DecodedJWT verifiedToken = verification
                    .build(() -> {
                        long currentMillis = clock.instant().toEpochMilli();
                        return new Date(currentMillis);
                    })
                    .verify(token);

            return Optional.of(verifiedToken);
        } catch (SignatureVerificationException | InvalidClaimException | TokenExpiredException e) {
            return Optional.empty();
        }
    }
}
