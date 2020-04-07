package de.borisskert.springjwt.user;

import de.borisskert.springjwt.user.persistence.UserEntity;
import de.borisskert.springjwt.vaidation.RawPassword;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Objects;

public class UserWithPassword {

    @Valid
    private final User user;

    @NotEmpty
    @RawPassword
    private final String rawPassword;

    private UserWithPassword(
            User user,
            String rawPassword
    ) {
        this.user = user;
        this.rawPassword = rawPassword;
    }

    public User getUser() {
        return user;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getRawPassword() {
        return rawPassword;
    }

    public UserEntity toEntityWithId(String id) {
        return user.toEntityWithId(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserWithPassword that = (UserWithPassword) o;
        return user.equals(that.user) &&
                rawPassword.equals(that.rawPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, rawPassword);
    }

    public static UserWithPassword from(User user, String rawPassword) {
        return new UserWithPassword(
                user,
                rawPassword
        );
    }
}
