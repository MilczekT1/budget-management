package pl.konradboniecki.budget.budgetmanagement.exception;

import pl.konradboniecki.chassis.exceptions.ResourceConflictException;

public class FamilyConflictException extends ResourceConflictException {
    public FamilyConflictException(String message) {
        super(message);
        this.printStackTrace();
    }
}
