Feature: User functionalities

  Background:
    Given our spring application is running

  Scenario: Getting no users
    When I ask for a user with Id "6e9f59fa-cc85-4096-9165-7a3661fd6bc0"
    Then I should return no users

  Scenario: Getting users
    Given A user exists with ID
      | Username   | Email                      | Day of Birth   | ID                                   |
      | smithj     | john.smith@fakemail.com    | 1990-10-03     | 6e9f59fa-cc85-4096-9165-7a3661fd6bc0 |
    When I ask for a user with Id "6e9f59fa-cc85-4096-9165-7a3661fd6bc0"
    Then I should get following user
      | Username   | Email                      | Day of Birth   |
      | smithj     | john.smith@fakemail.com    | 1990-10-03     |

  Scenario: Create user with generated ID
    When I create a user
      | Username   | Email                      | Day of Birth   |
      | mustermann | max.musterman@fakemail.com | 1945-05-08     |
    And I get the user location after creation
    And I ask for the user by location
    Then The location should get following user
      | Username   | Email                      | Day of Birth   |
      | mustermann | max.musterman@fakemail.com | 1945-05-08     |

  Scenario: Conflict when trying to create user with duplicate username
    Given A user exists with ID
      | Username   | Email                      | Day of Birth   | ID                                   |
      | paulN      | paul.noob@fakemail.com     | 1948-06-21     | 2884a717-5a17-49fa-84cc-d4321207c7f9 |
    When I create a user
      | Username   | Email                      | Day of Birth   |
      | paulN      | paul.pro@fakemail.com      | 1948-06-21     |
    Then I get a Conflict response

  Scenario: Conflict when trying to insert user with duplicate ID
    Given A user exists with ID
      | Username   | Email                      | Day of Birth   | ID                                   |
      | mincui     | mincui@fakemail.com        | 1948-06-21     | 9b686071-2973-4001-b0f9-6267422d45f7 |
    When I insert a user with ID
      | Username   | Email                      | Day of Birth   | ID                                   |
      | nimuic     | nimuic@fakemail.com        | 1948-06-21     | 9b686071-2973-4001-b0f9-6267422d45f7 |
    Then I get a Conflict response

  Scenario: Conflict when trying to insert user with duplicate username
    Given A user exists with ID
      | Username   | Email                      | Day of Birth   | ID                                   |
      | ashley     | ashley@fakemail.com        | 1962-07-08     | 47c0a2b5-927c-4189-b90d-8fb829a0b720 |
    When I insert a user with ID
      | Username   | Email                      | Day of Birth   | ID                                   |
      | ashley     | ashley@fakemail.com        | 1962-07-08     | 1ed47a5f-2793-41f8-ba1c-4a0f5c7e7c77 |
    Then I get a Conflict response
