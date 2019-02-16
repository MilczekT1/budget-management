@All
Feature: Jar Update

  Background:
    Given I'm authenticated with Basic Auth
    And I have already created a budget
    When I create jar with properties:
      | capacity | jarName  | budgetId             |
      | 5000.0   | holidays | {{myExistingBudget}} |
      | 100.0    | alcohol  | {{myExistingBudget}} |
    And jar is created

  Scenario: Update existing jar with IN-PROGRESS status
    Given I'm authenticated with Basic Auth
    When I update jar with name holidays by id from my budget with properties:
      | currentAmount | capacity | jarName  | budgetId             |
      | 4.0           | 5000.0   | holidays | {{myExistingBudget}} |
    Then the operation is a success
    And I search jar with name holidays by id from my budget
    And response contains jar with following properties
      | currentAmount | capacity | status      | jarName  | budgetId             |
      | 4.0           | 5000.0   | IN_PROGRESS | holidays | {{myExistingBudget}} |

  Scenario: Update existing jar to COMPLETED status
    Given I'm authenticated with Basic Auth
    When I update jar with name alcohol by id from my budget with properties:
      | currentAmount | capacity | jarName | budgetId             |
      | 100.0         | 100.0    | alcohol | {{myExistingBudget}} |
    Then the operation is a success
    And I search jar with name alcohol by id from my budget
    And response contains jar with following properties
      | currentAmount | capacity | status    | jarName | budgetId             |
      | 100.0         | 100.0    | COMPLETED | alcohol | {{myExistingBudget}} |

  Scenario: Update jar with inconsistent budget id in path and body
    Given I'm authenticated with Basic Auth
    When I update jar with name holidays by id from random budget with properties:
      | currentAmount | capacity | jarName  | budgetId             |
      | 4.0           | 5000.0   | holidays | {{myExistingBudget}} |
    Then operation is a failure

  Scenario: Update not existing jar
    Given I'm authenticated with Basic Auth
    When I update jar with name not_existing_jar by id from my budget with properties:
      | currentAmount | capacity | jarName  | budgetId             |
      | 4.0           | 5000.0   | holidays | {{myExistingBudget}} |
    Then jar is not found

  Scenario: Update jar from not existing budget
    Given I'm authenticated with Basic Auth
    When I update jar with name not_existing_jar by id from random budget with properties:
      | currentAmount | capacity | jarName  | budgetId         |
      | 4.0           | 5000.0   | holidays | {{randomBudget}} |
    Then jar is not found

  Scenario: Unauthorized user can't update expense
    Given I'm not authenticated
    When I update jar with name not_existing_jar by id from random budget with properties:
      | currentAmount | capacity | jarName  | budgetId             |
      | 4.0           | 5000.0   | holidays | {{myExistingBudget}} |
    Then the operation is unauthorized
