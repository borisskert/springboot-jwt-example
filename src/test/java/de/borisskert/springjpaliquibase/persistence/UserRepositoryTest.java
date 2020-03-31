package de.borisskert.springjpaliquibase.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext
@ActiveProfiles("IT")
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    public void shouldReturnEmptyOptionalForNotExistingId() throws Exception {
        String id = "a68d51f0-1490-404a-9f65-111e866c8678";
        Optional<UserEntity> maybe = repository.findById(id);

        assertThat(maybe.isEmpty(), is(equalTo(true)));
    }

    @Test
    public void shouldPersistUserWithId() throws Exception {
        String id = "6f2e4a96-7d51-49f8-896d-e8db83e0effa";

        UserEntity entityToSave = new UserEntity();
        entityToSave.setId(id);
        entityToSave.setUsername("The3aep1");
        entityToSave.setEmail("The3aep1@fakemail.com");
        entityToSave.setDateOfBirth(LocalDate.of(1987, 11, 15));

        repository.save(entityToSave);

        Optional<UserEntity> maybe = repository.findById(id);
        assertThat(maybe.isPresent(), is(equalTo(true)));

        UserEntity existingEntity = maybe.get();
        assertThat(existingEntity.getId(), is(equalTo(id)));
        assertThat(existingEntity.getUsername(), is(equalTo("The3aep1")));
        assertThat(existingEntity.getEmail(), is(equalTo("The3aep1@fakemail.com")));
        assertThat(existingEntity.getDateOfBirth(), is(equalTo(LocalDate.of(1987, 11, 15))));
    }

    @Test
    public void shouldNotPersistUserWithoutId() throws Exception {
        UserEntity entityToSave = new UserEntity();
        entityToSave.setUsername("my_username");
        entityToSave.setEmail("my@fakemail.com");
        entityToSave.setDateOfBirth(LocalDate.of(1987, 11, 15));

        assertThrows(JpaSystemException.class, () -> repository.save(entityToSave));
    }
}
