@All
Feature: Budget Searching

  Budget Search.

  Background:
    Given I'm authenticated with Basic Auth
    And family my_family already have a budget

  Scenario: Searching for existing budget
    Given I'm authenticated with Basic Auth
    When I get a budget for family my_family
    Then budget is found

  Scenario: Searching for not existing budget
    Given I'm authenticated with Basic Auth
    When I get a budget for family not_existing
    Then budget is not found

  Scenario: Unauthorized user can't find budget
    Given I'm not authenticated
    When I get a budget for family not_existing
    Then the operation is unauthorized
