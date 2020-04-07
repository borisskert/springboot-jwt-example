package de.borisskert.springjwt.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class FakeUserDetails implements UserDetails {
    private final String username;
    private final Collection<GrantedAuthority> authorities;

    FakeUserDetails(String username, Collection<GrantedAuthority> authorities) {
        this.username = username;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
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
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FakeUserDetails that = (FakeUserDetails) o;
        return username.equals(that.username) &&
                authorities.equals(that.authorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, authorities);
    }

    public static FakeUserDetails of(String username, Collection<String> roles) {
        assert username != null;
        assert roles != null;

        Collection<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());

        return new FakeUserDetails(username, authorities);
    }
}
