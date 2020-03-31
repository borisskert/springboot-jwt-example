package de.borisskert.springjpaliquibase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

@SpringBootTest
@DirtiesContext
@ActiveProfiles("IT")
class UserServiceTest {

    @Autowired
    private UserService service;

    @Test
    public void shouldReturnEmptyForNotExistingId() throws Exception {
        Optional<User> maybe = service.getUserById("my_unknown_id");
        assertThat(maybe.isEmpty(), is(equalTo(true)));
    }

    @Test
    public void shouldReturnUserIfSavedPreviously() throws Exception {
        User user = MockUsers.USER_ONE;

        String createdId = service.create(user);
        Optional<User> maybe = service.getUserById(createdId);

        assertThat(maybe.isPresent(), is(equalTo(true)));

        User actual = maybe.get();
        assertThat(actual, is(equalTo(user)));
        assertThat(actual, is(not(sameInstance(user))));
    }

    @Test
    public void shouldInsertUserWithSpecifiedId() throws Exception {
        String id = MockUsers.USER_ID_TO_INSERT;
        User user = MockUsers.USER_TO_INSERT;

        service.insert(id, user);
        Optional<User> maybe = service.getUserById(id);

        assertThat(maybe.isPresent(), is(equalTo(true)));

        User actual = maybe.get();
        assertThat(actual, is(equalTo(user)));
        assertThat(actual, is(not(sameInstance(user))));
    }
}
