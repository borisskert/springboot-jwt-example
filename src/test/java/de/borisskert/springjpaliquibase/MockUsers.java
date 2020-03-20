package de.borisskert.springjpaliquibase;

import java.time.LocalDate;

public class MockUsers {

    public static final String USER_ONE_ID = "6e9f59fa-cc85-4096-9165-7a3661fd6bc0";
    public static final User USER_ONE = new User("my_username", "my@fakemail.com", LocalDate.of(1990, 10, 3));
    public static final User USER_TO_CREATE = new User("created_user", "my_other@fakemail.com", LocalDate.of(1945, 5, 8));

    public static final String USER_ID_TO_INSERT = "2884a717-5a17-49fa-84cc-d4321207c7f9";
    public static final User USER_TO_INSERT = new User("user_to_insert", "user_to_insert@fakemail.com", LocalDate.of(1948, 6, 21));

    public static final String NOT_EXISTING_ID = "9b686071-2973-4001-b0f9-6267422d45f7";

    public static final User USER_WITH_DUPLICATE_USERNAME = new User("duplicate", "my@fakemail.com", LocalDate.of(1962, 7, 8));
}
