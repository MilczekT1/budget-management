@All
Feature: Expense Searching

  Background:
    Given I'm authenticated with Basic Auth
    And I have already created a budget
    When I create expense with properties:
      | amount | comment  | budgetId             |
      | 1.0    | comment1 | {{myExistingBudget}} |
      | 2.0    | comment2 | {{myExistingBudget}} |
    And expense is created

  # Single expense: /api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}

  Scenario: Get existing expense
    Given I'm authenticated with Basic Auth
    When I search expense with comment comment1 by id from my budget
    Then expense is found
    And response contains expense with following properties
      | amount | comment  | budgetId             |
      | 1.0    | comment1 | {{myExistingBudget}} |

  Scenario: Get not existing expense
    Given I'm authenticated with Basic Auth
    When I search expense with comment not_existing_expense by id from my budget
    Then expense is not found

  Scenario: Get expense from not existing budget
    Given I'm authenticated with Basic Auth
    When I search expense with comment notExistingComment by id from random budget
    Then expense is not found

  Scenario: Unauthorized user can't find expense
    Given I'm not authenticated
    When I search expense with comment not_existing_expense by id from my budget
    Then the operation is unauthorized

  # Multiple expenses: /api/budget-mgt/v1/budgets/{budgetId}/expenses

  Scenario: Get existing expenses from budget
    Given I'm authenticated with Basic Auth
    When I search expenses from my budget
    Then expenses are found
    And response contains 2 expenses

  Scenario: Get expenses from budget without expenses
    Given I'm authenticated with Basic Auth
    And I delete expense with comment comment1 by id from my budget
    And I delete expense with comment comment2 by id from my budget
    When I search expenses from my budget
    Then expenses no longer exist

  Scenario: Get expenses from not existing budget
    Given I'm authenticated with Basic Auth
    When I search expenses from random budget
    Then budget is not found

  Scenario: Unauthorized user can't find expenses from budget
    Given I'm not authenticated
    When I search expenses from my budget
    Then the operation is unauthorized
