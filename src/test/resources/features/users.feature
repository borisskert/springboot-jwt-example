Feature: User functionalities

  Background:
    Given our spring application is running
    When I log in as
      | Username | Password |
      | admin    | admin123 |

  Scenario: Getting no users
    When I ask for a user with Id "6e9f59fa-cc85-4096-9165-7a3661fd6bc0"
    Then I should return no users

  Scenario: Getting users
    Given A user exists with ID
      | Username   | Email                      | Day of Birth   | ID                                   | Roles |
      | smithj     | john.smith@fakemail.com    | 1990-10-03     | 6e9f59fa-cc85-4096-9165-7a3661fd6bc0 | USER  |
    When I ask for a user with Id "6e9f59fa-cc85-4096-9165-7a3661fd6bc0"
    Then I should get following user
      | Username   | Email                      | Day of Birth   | Roles |
      | smithj     | john.smith@fakemail.com    | 1990-10-03     | USER  |

  Scenario: Create user with generated ID
    When I create a user
      | Username   | Email                      | Day of Birth   | Roles |
      | mustermann | max.musterman@fakemail.com | 1945-05-08     | USER  |
    And I get the user location after creation
    And I ask for the user by location
    Then The location should get following user
      | Username   | Email                      | Day of Birth   | Roles |
      | mustermann | max.musterman@fakemail.com | 1945-05-08     | USER  |

  Scenario: Conflict when trying to create user with duplicate username
    Given A user exists with ID
      | Username   | Email                      | Day of Birth   | ID                                   | Roles |
      | paulNo     | paul.noob@fakemail.com     | 1948-06-21     | 2884a717-5a17-49fa-84cc-d4321207c7f9 | USER  |
    When I create a user
      | Username   | Email                      | Day of Birth   | Roles |
      | paulNo     | paul.pro@fakemail.com      | 1948-06-21     | USER  |
    Then I get a Conflict response

  Scenario: Conflict when trying to insert user with duplicate ID
    Given A user exists with ID
      | Username   | Email                      | Day of Birth   | ID                                   | Roles |
      | mincui     | mincui@fakemail.com        | 1948-06-21     | 9b686071-2973-4001-b0f9-6267422d45f7 | USER  |
    When I insert a user with ID
      | Username   | Email                      | Day of Birth   | ID                                   | Roles |
      | nimuic     | nimuic@fakemail.com        | 1948-06-21     | 9b686071-2973-4001-b0f9-6267422d45f7 | USER  |
    Then I get a Conflict response

  Scenario: Conflict when trying to insert user with duplicate username
    Given A user exists with ID
      | Username   | Email                      | Day of Birth   | ID                                   | Roles |
      | ashley     | ashley@fakemail.com        | 1962-07-08     | 47c0a2b5-927c-4189-b90d-8fb829a0b720 | USER  |
    When I insert a user with ID
      | Username   | Email                      | Day of Birth   | ID                                   | Roles |
      | ashley     | ashley@fakemail.com        | 1962-07-08     | 1ed47a5f-2793-41f8-ba1c-4a0f5c7e7c77 | USER  |
    Then I get a Conflict response

  Scenario: Get all users
    When I ask for all users
    Then I should get following users
      | Username   | Email                      | Day of Birth   | Roles |
      | admin      | admin@localhost            | 1970-01-01     | ADMIN |
      | ashley     | ashley@fakemail.com        | 1962-07-08     | USER  |
      | mincui     | mincui@fakemail.com        | 1948-06-21     | USER  |
      | mustermann | max.musterman@fakemail.com | 1945-05-08     | USER  |
      | paulNo     | paul.noob@fakemail.com     | 1948-06-21     | USER  |
      | smithj     | john.smith@fakemail.com    | 1990-10-03     | USER  |

  Scenario: Sign up
    When I sign up as user
      | Username   | Email                      | Day of Birth   | Password    |
      | Buu1eeVu   | Buu1eeVu@fakemail.com      | 1989-10-18     | my_p@ssw0rd |
    And I get the user location after sign-up
    And I ask for the user by location
    Then The location should get following user
      | Username   | Email                      | Day of Birth   | Roles |
      | Buu1eeVu   | Buu1eeVu@fakemail.com      | 1989-10-18     | USER  |

  Scenario: Try to sign up second time
    When I sign up as user
      | Username   | Email                      | Day of Birth   | Password    |
      | Buu1eeVu   | Buu1eeVu@fakemail.com      | 1989-10-18     | my_p@ssw0rd |
    Then I get a Conflict response
