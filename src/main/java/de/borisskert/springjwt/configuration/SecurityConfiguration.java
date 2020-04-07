package de.borisskert.springjwt.configuration;

import com.auth0.spring.security.api.JwtWebSecurityConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.borisskert.springjwt.authentication.JwtTokenService;
import de.borisskert.springjwt.authentication.userdetails.RepositoryUserDetailsService;
import de.borisskert.springjwt.authentication.filter.JwtAuthenticationFilter;
import de.borisskert.springjwt.authentication.filter.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private RepositoryUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        JwtWebSecurityConfigurer
                .forRS256("apiAudience", "issuer")
                .configure(http)
                .cors()
                    .and()
                .csrf()
                    .disable()
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                    .antMatchers(HttpMethod.GET, "/api/users/me").hasAnyRole("ADMIN", "USER")
                    .antMatchers(HttpMethod.GET, "/api/users/{id}").hasRole("ADMIN")
                    .antMatchers(HttpMethod.POST, "/api/users/sign-up").permitAll()
                    .antMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                    .antMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                    .antMatchers(HttpMethod.PATCH, "/api/users/**").permitAll()
                    .antMatchers(HttpMethod.DELETE, "/api/users/**").permitAll()
                    .anyRequest().authenticated()
                    .and()

                .addFilter(new JwtAuthenticationFilter(authenticationManager, objectMapper, jwtTokenService))
                .addFilter(new JwtAuthorizationFilter(authenticationManager, jwtTokenService))

                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // @formatter:on
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    /*
     * https://stackoverflow.com/a/21639553
     */
    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
