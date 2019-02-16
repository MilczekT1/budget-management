@All
Feature: Expense Deletion

  Background:
    Given I'm authenticated with Basic Auth
    And I have already created a budget
    When I create expense with properties:
      | amount | comment | budgetId             |
      | 1.0    | comment | {{myExistingBudget}} |
    And expense is created

  Scenario: Delete existing expense
    Given I'm authenticated with Basic Auth
    When I delete expense with comment comment by id from my budget
    Then expense is deleted

  Scenario: Delete not existing expense
    Given I'm authenticated with Basic Auth
    When I delete expense with comment not_existing by id from my budget
    Then expense is not found

  Scenario: Delete expense from not existing budget
    Given I'm authenticated with Basic Auth
    When I delete expense with comment not_existing_budget by id from random budget
    Then budget is not found

  Scenario: Unauthorized user can't create expense
    Given I'm not authenticated
    When I delete expense with comment not_existing_expense by id from my budget
    Then the operation is unauthorized
