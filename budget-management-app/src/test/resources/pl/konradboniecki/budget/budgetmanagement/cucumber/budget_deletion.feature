@All
Feature: Budget Deletion

  Budget deletion.

  Background:
    Given I'm authenticated with Basic Auth
    And family my_family already have a budget

  Scenario: Delete existing budget
    Given I'm authenticated with Basic Auth
    When I delete a budget for family my_family
    Then budget is deleted

  Scenario: Delete not existing budget
    Given I'm authenticated with Basic Auth
    When I delete a budget for family not_existing
    Then budget is not found

  Scenario: Unauthorized user can't delete budget
    Given I'm not authenticated
    When I delete a budget for family not_existing
    Then the operation is unauthorized
