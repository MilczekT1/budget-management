@All
Feature: Expense Update

  Background:
    Given I'm authenticated with Basic Auth
    And I have already created a budget
    When I create expense with properties:
      | amount | comment | budgetId             |
      | 1.0    | comment | {{myExistingBudget}} |
    And expense is created

  Scenario: Update existing expense
    Given I'm authenticated with Basic Auth
    When I update expense with comment comment by id from my budget with properties:
      | amount | comment | budgetId             |
      | 2.0    | comment | {{myExistingBudget}} |
    Then the operation is a success
    And I search expense with comment comment by id from my budget
    And response contains expense with following properties
      | amount | comment | budgetId             |
      | 2.0    | comment | {{myExistingBudget}} |

  Scenario: Update expense with inconsistent budget id in path and body
    Given I'm authenticated with Basic Auth
    When I update expense with comment comment by id from random budget with properties:
      | amount | comment | budgetId             |
      | 5.0    | comment | {{myExistingBudget}} |
    Then operation is a failure

  Scenario: Update not existing expense
    Given I'm authenticated with Basic Auth
    When I update expense with comment not_existing_comment by id from my budget with properties:
      | amount | comment | budgetId             |
      | 5.0    | comment | {{myExistingBudget}} |
    Then expense is not found

  Scenario: Unauthorized user can't update expense
    Given I'm not authenticated
    When I update expense with comment comment by id from random budget with properties:
      | amount | comment | budgetId             |
      | 2.0    | comment | {{myExistingBudget}} |
    Then the operation is unauthorized
