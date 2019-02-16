package pl.konradboniecki.budget.budgetmanagement.cucumber.commons;

import io.cucumber.java.DataTableType;
import pl.konradboniecki.budget.openapi.dto.model.OASBudget;
import pl.konradboniecki.budget.openapi.dto.model.OASExpense;
import pl.konradboniecki.budget.openapi.dto.model.OASJar;

import java.util.Map;

public class DataTableConverter {

    /*
    | familyId | maxJars |
    | 0        | 0       |
    */
    @DataTableType
    public OASBudget budgetRow(Map<String, String> entry) {
        String familyIdEntry = entry.get("familyId");
        if (familyIdEntry.startsWith("{{")) {
            familyIdEntry = familyIdEntry.substring(2, familyIdEntry.length() - 2);
            familyIdEntry = SharedData.getFamilyNameToIdMap().get(familyIdEntry);
        }
        return new OASBudget()
                .familyId(familyIdEntry)
                .maxJars(Long.valueOf(entry.getOrDefault("maxJars", "0")));
    }

    /*
    | amount | comment | budgetId             |
    | 1.0    | comment | {{myExistingBudget}} |
    */
    @DataTableType
    public OASExpense expenseRow(Map<String, String> entry) {
        String budgetId;
        if (entry.get("budgetId").equals("{{myExistingBudget}}")) {
            budgetId = SharedData.myExistingBudget.getId();
        } else {
            budgetId = entry.getOrDefault("budgetId", null);
        }
        return new OASExpense()
                .amount(Double.valueOf(entry.getOrDefault("amount", "0")))
                .comment(entry.getOrDefault("comment", null))
                .budgetId(budgetId);
    }

    /*
    | currentAmount | capacity     | jarName  | status      | budgetId                            |
    | 36.0          | 5000.0       | holidays | IN_PROGRESS | d1c888ab-b812-42cf-94ff-353ebb6f6278 |
    */
    @DataTableType
    public OASJar jarRow(Map<String, String> entry) {
        String budgetId = getMyOrRandomBudgetId(entry);
        return new OASJar()
                .capacity(Double.valueOf(entry.getOrDefault("capacity", "0")))
                .currentAmount(Double.valueOf(entry.getOrDefault("currentAmount", "0")))
                .jarName(entry.getOrDefault("jarName", null))
                .budgetId(budgetId);
    }

    private String getMyOrRandomBudgetId(Map<String, String> entry) {
        if (entry.get("budgetId").equals("{{myExistingBudget}}")) {
            return SharedData.myExistingBudget.getId();
        } else if (entry.get("budgetId").equals("{{randomBudget}}")) {
            return SharedData.getLastCommonRandomIdOrGenerateNewIfEmpty();
        } else {
            return entry.getOrDefault("budgetId", null);
        }
    }
}
