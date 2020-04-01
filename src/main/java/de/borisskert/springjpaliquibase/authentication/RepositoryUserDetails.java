package de.borisskert.springjpaliquibase.authentication;

import de.borisskert.springjpaliquibase.persistence.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

class RepositoryUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final Collection<GrantedAuthority> roles;

    private RepositoryUserDetails(String username, String password, Collection<GrantedAuthority> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return password != null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static RepositoryUserDetails fromEntity(UserEntity entity) {
        Collection<GrantedAuthority> roles = entity.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());

        return new RepositoryUserDetails(entity.getUsername(), entity.getPassword(), roles);
    }
}
