@All
Feature: Jar Creation

  Scenario: Create jar for existing budget
    Given I'm authenticated with Basic Auth
    And I have already created a budget
    When I create jar with properties:
      | capacity | jarName  | budgetId             |
      | 5000.0   | holidays | {{myExistingBudget}} |
    Then jar is created
    And response contains jar with following properties
      | currentAmount | capacity | status      | jarName  | budgetId             |
      | 0.0           | 5000.0   | NOT_STARTED | holidays | {{myExistingBudget}} |

  Scenario: Create jar for not existing budget
    Given I'm authenticated with Basic Auth
    When I create jar with properties:
      | capacity | jarName  | budgetId                            |
      | 5000.0   | holidays | 1c888ab-b812-42cf-94ff-353ebb6f6278 |
    Then budget is not found

  Scenario: Unauthorized user can't create jar
    Given I'm not authenticated
    When I create jar with properties:
      | capacity | jarName  | budgetId                             |
      | 5000.0   | holidays | d1c888ab-b812-42cf-94ff-353ebb6f6278 |
    Then the operation is unauthorized
