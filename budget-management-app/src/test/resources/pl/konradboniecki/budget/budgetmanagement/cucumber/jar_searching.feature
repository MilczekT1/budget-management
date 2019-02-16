@All
Feature: Jar Searching

  Background:
    Given I'm authenticated with Basic Auth
    And I have already created a budget
    When I create jar with properties:
      | capacity | jarName  | budgetId             |
      | 5000.0   | holidays | {{myExistingBudget}} |
      | 100.0    | alcohol  | {{myExistingBudget}} |
    And jar is created

  # Single jar: /api/budget-mgt/v1/budgets/{budgetId}/jars/{jarId}

  Scenario: Get existing jar
    Given I'm authenticated with Basic Auth
    When I search jar with name holidays by id from my budget
    Then jar is found
    And response contains jar with following properties
      | currentAmount | capacity | jarName  | budgetId             |
      | 0.0           | 5000.0   | holidays | {{myExistingBudget}} |

  Scenario: Get not existing jar
    Given I'm authenticated with Basic Auth
    When I search jar with name not_existing_jar by id from my budget
    Then jar is not found
#
  Scenario: Get jar from not existing budget
    Given I'm authenticated with Basic Auth
    When I search jar with name not_existing_budget by id from random budget
    Then jar is not found

  Scenario: Unauthorized user can't find jar
    Given I'm not authenticated
    When I search jar with name not_existing_jar by id from my budget
    Then the operation is unauthorized

  # Multiple jars: /api/budget-mgt/v1/budgets/{budgetId}/jars

  Scenario: Get existing jars from budget
    Given I'm authenticated with Basic Auth
    When I search jars from my budget
    Then jars are found
    And response contains 2 jars

  Scenario: Get jars from budget without jars
    Given I'm authenticated with Basic Auth
    And I delete jar with name holidays by id from my budget
    And I delete jar with name alcohol by id from my budget
    When I search jars from my budget
    Then jars no longer exist
#
  Scenario: Get jars from not existing budget
    Given I'm authenticated with Basic Auth
    When I search expenses from random budget
    Then jar is not found
#
  Scenario: Unauthorized user can't find jars from budget
    Given I'm not authenticated
    When I search jars from my budget
    Then the operation is unauthorized
