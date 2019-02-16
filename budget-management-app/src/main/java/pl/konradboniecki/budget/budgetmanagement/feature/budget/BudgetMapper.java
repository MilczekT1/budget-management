package pl.konradboniecki.budget.budgetmanagement.feature.budget;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.openapi.dto.model.OASBudget;
import pl.konradboniecki.budget.openapi.dto.model.OASBudgetCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedBudget;

@Service
public class BudgetMapper {

    Budget toBudget(@NonNull OASBudgetCreation budgetCreation) {
        return new Budget()
                .setFamilyId(budgetCreation.getFamilyId())
                .setMaxJars(budgetCreation.getMaxJars());
    }

    public OASBudget toOASBudget(@NonNull Budget budget) {
        return new OASBudget()
                .id(budget.getId())
                .familyId(budget.getFamilyId())
                .maxJars(budget.getMaxJars());
    }

    public OASBudgetCreation toOASBudgetCreation(@NonNull Budget budget) {
        return new OASBudgetCreation()
                .familyId(budget.getFamilyId())
                .maxJars(budget.getMaxJars());
    }

    public OASCreatedBudget toOASCreatedBudget(@NonNull Budget budget) {
        return new OASCreatedBudget()
                .id(budget.getId())
                .familyId(budget.getFamilyId())
                .maxJars(budget.getMaxJars());
    }
}
