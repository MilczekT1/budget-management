@All
Feature: Jar Deletion

  Background:
    Given I'm authenticated with Basic Auth
    And I have already created a budget
    When I create jar with properties:
      | capacity | jarName  | budgetId             |
      | 5000.0   | holidays | {{myExistingBudget}} |
      | 100.0    | alcohol  | {{myExistingBudget}} |
    Then jar is created

  Scenario: Delete jar
    Given I'm authenticated with Basic Auth
    When I delete jar with name holidays by id from my budget
    Then jar is deleted

  Scenario: Delete not existing jar
    Given I'm authenticated with Basic Auth
    When I delete jar with name not_existing_jar by id from my budget
    Then jar is not found

  Scenario: Delete jar from not existing budget
    Given I'm authenticated with Basic Auth
    When I delete jar with name not_existing_budget by id from random budget
    Then budget is not found

  Scenario: Unauthorized user can't delete jar
    Given I'm not authenticated
    When I delete jar with name not_existing_budget by id from my budget
    Then the operation is unauthorized

