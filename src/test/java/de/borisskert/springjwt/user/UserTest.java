package de.borisskert.springjwt.user;

import de.borisskert.springjwt.user.persistence.UserEntity;
import de.borisskert.springjwt.user.User;
import de.borisskert.springjwt.user.UserWithPassword;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class UserTest {

    @Test
    public void shouldConvertToEntity() throws Exception {
        User user = User.from("my_username", "mail@fakemail.com", LocalDate.of(1984, 5, 31));

        UserEntity entity = user.toEntity();

        assertThat(entity.getUsername(), is(equalTo("my_username")));
        assertThat(entity.getEmail(), is(equalTo("mail@fakemail.com")));
        assertThat(entity.getDateOfBirth(), is(equalTo(LocalDate.of(1984, 5, 31))));
    }

    @Test
    public void shouldConvertToEntityWithId() throws Exception {
        User user = User.from("my_username", "mail@fakemail.com", LocalDate.of(1984, 5, 31));

        UserEntity entity = user.toEntityWithId("my_id");

        assertThat(entity.getId(), is(equalTo("my_id")));
        assertThat(entity.getUsername(), is(equalTo("my_username")));
        assertThat(entity.getEmail(), is(equalTo("mail@fakemail.com")));
        assertThat(entity.getDateOfBirth(), is(equalTo(LocalDate.of(1984, 5, 31))));
    }

    @Test
    public void shouldConvertFromEntity() throws Exception {
        UserEntity entity = new UserEntity();
        entity.setUsername("my_username");
        entity.setEmail("mail@fakemail.com");
        entity.setDateOfBirth(LocalDate.of(1984, 5, 31));
        entity.setRoles(List.of("ADMIN", "USER"));

        User user = User.fromEntity(entity);

        assertThat(user.getUsername(), is(equalTo("my_username")));
        assertThat(user.getEmail(), is(equalTo("mail@fakemail.com")));
        assertThat(user.getDateOfBirth(), is(equalTo(LocalDate.of(1984, 5, 31))));
        assertThat(user.getRoles(), containsInAnyOrder("ADMIN", "USER"));
    }

    @Test
    public void shouldTransferToUserWithPassword() throws Exception {
        User user = User.from("my_username", "mail@fakemail.com", LocalDate.of(1984, 5, 31));

        UserWithPassword userWithPassword = user.withPassword("my password");

        assertThat(userWithPassword.getUser(), is(equalTo(user)));
        assertThat(userWithPassword.getRawPassword(), is(equalTo("my password")));
    }
}
