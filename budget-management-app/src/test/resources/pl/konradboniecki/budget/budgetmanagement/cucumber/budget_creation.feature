@All
Feature: Budget Creation

  Each family can only have 1 budget.

  Scenario: Create budget for new family
    Given I'm authenticated with Basic Auth
    And family my_family doesn't have a budget
    When I create a budget for family my_family with properties:
      | familyId      | maxJars |
      | {{my_family}} | 6       |
    Then budget is created

  Scenario: Only 1 budget can be assigned to family
    Given I'm authenticated with Basic Auth
    And family my_family already have a budget
    When I create a budget for family my_family with properties:
      | familyId      | maxJars |
      | {{my_family}} | 6       |
    Then budget is not created

  Scenario: Unauthorized user can't create budget
    Given I'm not authenticated
    When I create a budget for family any with properties:
      | familyId | maxJars |
      | {{any}}  | 0       |
    Then the operation is unauthorized
