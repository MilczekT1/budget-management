package pl.konradboniecki.budget.budgetmanagement.feature.jar;

import lombok.Getter;
import lombok.Setter;

enum JarStatus {
    COMPLETED("COMPLETED"),
    IN_PROGRESS("IN PROGRESS"),
    NOT_STARTED("NOT STARTED");

    JarStatus(String status){
        setStatus(status);
    }

    @Getter
    @Setter
    private String status;
}
