@All
Feature: Expense Creation

  Scenario: Create new expense for existing budget
    Given I'm authenticated with Basic Auth
    And I have already created a budget
    When I create expense with properties:
      | amount | comment | budgetId             |
      | 1.0    | comment | {{myExistingBudget}} |
    Then expense is created

  Scenario: Create new expense for not existing budget
    Given I'm authenticated with Basic Auth
    When I create expense with properties:
      | amount | comment | budgetId                             |
      | 1.0    | comment | d1c888ab-b812-42cf-94ff-353ebb6f6278 |
    Then budget is not found

  Scenario: Unauthorized user can't create expense
    Given I'm not authenticated
    When I create expense with properties:
      | amount | comment | budgetId                             |
      | 1.0    | comment | d1c888ab-b812-42cf-94ff-353ebb6f6278 |
    Then the operation is unauthorized
