package de.borisskert.springjwt.user;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MockUsers {

    public static final List<String> USER_ROLE = List.of("USER");
    public static final String USER_ONE_ID = "6e9f59fa-cc85-4096-9165-7a3661fd6bc0";

    public static final User USER_ONE = User.from(
            "my_username",
            "my@fakemail.com",
            LocalDate.of(1990, 10, 3),
            USER_ROLE
    );

    public static final String USER_ID_TO_INSERT = "2884a717-5a17-49fa-84cc-d4321207c7f9";
    public static final User USER_TO_INSERT = User.from(
            "user_insert",
            "user_to_insert@fakemail.com",
            LocalDate.of(1948, 6, 21),
            USER_ROLE
    );

    public static final String VALID_PASSWORD = "my_p@ssw0rd";

    public static final UserToSignUp USER_TO_SIGN_UP = UserToSignUp.from(
            "sign_up",
            "user_to_sign_up@fakemail.com",
            LocalDate.of(1943, 11, 29),
            VALID_PASSWORD
    );
}
