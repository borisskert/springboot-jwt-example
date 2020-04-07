package de.borisskert.springjwt.user;

import de.borisskert.springjwt.user.User;
import de.borisskert.springjwt.user.UserToSignUp;

import java.time.LocalDate;
import java.util.Map;

public class MockUsers {

    public static final String USER_ONE_ID = "6e9f59fa-cc85-4096-9165-7a3661fd6bc0";
    public static final User USER_ONE = User.from("my_username", "my@fakemail.com", LocalDate.of(1990, 10, 3));
    public static final User USER_TO_CREATE = User.from("created_user", "my_other@fakemail.com", LocalDate.of(1945, 5, 8));

    public static final String USER_ID_TO_INSERT = "2884a717-5a17-49fa-84cc-d4321207c7f9";
    public static final User USER_TO_INSERT = User.from("user_insert", "user_to_insert@fakemail.com", LocalDate.of(1948, 6, 21));

    public static final String NOT_EXISTING_ID = "9b686071-2973-4001-b0f9-6267422d45f7";

    public static final User USER_WITH_DUPLICATE_USERNAME = User.from("duplicate", "my@fakemail.com", LocalDate.of(1962, 7, 8));

    public static final String VALID_PASSWORD = "my_p@ssw0rd";

    public static final UserToSignUp USER_TO_SIGN_UP = UserToSignUp.from(
            "sign_up", "user_to_sign_up@fakemail.com", LocalDate.of(1943, 11, 29), VALID_PASSWORD
    );

    public static final Map<String, String> USER_TO_SIGN_UP_AS_MAP = Map.of(
            "username", "sign_up2",
            "email", "user_to_sign_up@fakemail.com",
            "dateOfBirth", "1943-11-29",
            "rawPassword", VALID_PASSWORD
    );

    public static final UserToSignUp USER_TO_SIGN_UP_WITH_DUPLICATE_USERNAME = UserToSignUp.from(
            "duplicate2", "user_to_sign_up_duplicate@fakemail.com", LocalDate.of(1943, 11, 29), VALID_PASSWORD
    );
}
