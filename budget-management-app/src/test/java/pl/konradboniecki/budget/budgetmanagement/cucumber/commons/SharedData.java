package pl.konradboniecki.budget.budgetmanagement.cucumber.commons;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.konradboniecki.budget.openapi.dto.model.OASBudget;
import pl.konradboniecki.budget.openapi.dto.model.OASExpense;
import pl.konradboniecki.budget.openapi.dto.model.OASJar;

import java.util.*;

@Data
@Component
public class SharedData {
    /*
     Replacement for {{myExistingBudget}}
     */
    @Setter
    public static OASBudget myExistingBudget;
    private ResponseEntity<?> lastResponseEntity;
    /**
     * Purpose of this id isto have consistent "random"
     * id with body and path between multiple steps
     */
    @Setter
    public static String lastCommonRandomId;

    public static String getLastCommonRandomIdOrGenerateNewIfEmpty() {
        if (lastCommonRandomId == null) {
            lastCommonRandomId = UUID.randomUUID().toString();
        }
        return lastCommonRandomId;
    }

    @Getter
    private static Map<String, String> familyNameToIdMap = new HashMap<>();

    public String getFamilyIdForName(String familyName) {
        familyNameToIdMap.putIfAbsent(familyName, UUID.randomUUID().toString());
        return familyNameToIdMap.get(familyName);
    }

    /*
        Budget
     */

    private List<String> budgetIdsToDelete = new LinkedList<>();

    public void addBudgetIdToDelete(String budgetId) {
        budgetIdsToDelete.add(budgetId);
    }

    public void clearBudgetIdsToDelete() {
        budgetIdsToDelete.clear();
    }

    /*
        Expense
     */

    private Map<String, String> expenseIdToBudgetIdMapToDelete = new HashMap<>();
    private Map<String, OASExpense> commentToExpenseMap = new HashMap<>();

    public void addExpenseIdToBudgetIdEntry(String expenseId, String budgetId) {
        expenseIdToBudgetIdMapToDelete.putIfAbsent(expenseId, budgetId);
    }

    public void clearExpenseIdToBudgetIdMapToDelete() {
        expenseIdToBudgetIdMapToDelete.clear();
    }

    public void addCommentToExpenseEntry(String key, OASExpense value) {
        commentToExpenseMap.putIfAbsent(key, value);
    }

    public void clearCommentToExpenseId() {
        commentToExpenseMap.clear();
    }

    /*
        Jar
     */

    private Map<String, OASJar> jarNameToJarMap = new HashMap<>();
    private Map<String, String> jarIdToBudgetIdMapToDelete = new HashMap<>();

    public void addJarNameToJarEntry(String key, OASJar value) {
        jarNameToJarMap.putIfAbsent(key, value);
    }

    public void clearJarNameToJarMap() {
        jarNameToJarMap.clear();
    }

    public void addJarIdToBudgetIdEntry(String jarId, String budgetId) {
        jarIdToBudgetIdMapToDelete.put(jarId, budgetId);
    }

    public void clearJarIdToBudgetIdMapToDelete() {
        jarIdToBudgetIdMapToDelete.clear();
    }
}
