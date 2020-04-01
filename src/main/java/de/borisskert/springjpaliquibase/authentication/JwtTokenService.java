package de.borisskert.springjpaliquibase.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Service
public class JwtTokenService {

    public String createSignedTokenFor(UserDetails credentials) {
        Date expiresAt = new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME);
        Algorithm hmac512 = HMAC512(SecurityConstants.SECRET.getBytes());

        List<String> roles = credentials.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableList());

        return JWT.create()
                .withSubject(credentials.getUsername())
                .withClaim("roles", roles)
                .withExpiresAt(expiresAt)
                .sign(hmac512);
    }

    public Authentication authenticate(String tokenValue) {
        DecodedJWT decodedJwt = parseJwt(tokenValue);
        String user = decodedJwt.getSubject();

        Set<SimpleGrantedAuthority> roles = decodedJwt.getClaim("roles")
                .asList(String.class)
                .stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());

        if (user != null) {
            return new UsernamePasswordAuthenticationToken(user, null, roles);
        }

        return null;
    }

    private DecodedJWT parseJwt(String token) {
        byte[] secretInBytes = SecurityConstants.SECRET.getBytes();
        Algorithm hmacSha512 = Algorithm.HMAC512(secretInBytes);

        return JWT.require(hmacSha512)
                .build()
                .verify(token);
    }
}
