package de.borisskert.springjwt.authentication;

import de.borisskert.springjwt.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RepositoryUserDetailsService implements UserDetailsService {
    private final UserRepository repository;

    @Autowired
    public RepositoryUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findOneByUsername(username)
                .map(RepositoryUserDetails::fromEntity)
                .orElse(null);
    }
}
