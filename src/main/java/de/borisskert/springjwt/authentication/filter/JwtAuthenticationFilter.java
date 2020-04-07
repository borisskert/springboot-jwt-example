package de.borisskert.springjwt.authentication.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.borisskert.springjwt.authentication.JwtTokenService;
import de.borisskert.springjwt.authentication.SecurityConstants;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper mapper;

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, ObjectMapper mapper, JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.mapper = mapper;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        try {
            UserCredentials credentials = mapper.readValue(request.getInputStream(), UserCredentials.class);

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.getUsername(),
                            credentials.getPassword(),
                            List.of())
            );
        } catch (IOException e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authentication
    ) {
        UserDetails credentials = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenService.createSignedTokenFor(credentials);

        response.addHeader(SecurityConstants.HEADER_KEY, SecurityConstants.TOKEN_PREFIX + token);
    }

    private static class UserCredentials {
        private final String username;
        private final String password;

        private UserCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
