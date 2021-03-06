package de.borisskert.springjwt.authentication.filter;

import de.borisskert.springjwt.authentication.jwt.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static de.borisskert.springjwt.authentication.SecurityConstants.HEADER_KEY;
import static de.borisskert.springjwt.authentication.SecurityConstants.TOKEN_PREFIX;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthorizationFilter(AuthenticationManager authManager, JwtTokenService jwtTokenService) {
        super(authManager);
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        Authentication authentication = extractAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }

    private Authentication extractAuthentication(HttpServletRequest request) {
        String headerValue = request.getHeader(HEADER_KEY);
        return Optional.ofNullable(headerValue)
                .flatMap(this::toAuthenticationToken)
                .orElse(null);
    }

    private Optional<Authentication> toAuthenticationToken(String headerValue) {
        String tokenValue = extractToken(headerValue);
        return jwtTokenService.tryToAuthenticate(tokenValue);
    }

    private String extractToken(String headerValue) {
        return headerValue.replace(TOKEN_PREFIX, "");
    }
}
