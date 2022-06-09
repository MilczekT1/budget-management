package pl.konradboniecki.budget.budgetmanagement.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.konradboniecki.budget.budgetmanagement.feature.expense.ExpenseService;
import pl.konradboniecki.budget.openapi.api.ExpenseAssociationApi;
import pl.konradboniecki.budget.openapi.dto.model.*;

@AllArgsConstructor
@RestController
public class ExpenseController implements ExpenseAssociationApi {

    private final ExpenseService expenseService;

    @Override
    public ResponseEntity<OASCreatedExpense> createExpense(String budgetId, OASExpenseCreation oaSExpenseCreation) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(expenseService.saveExpense(oaSExpenseCreation, budgetId));
    }

    @Override
    public ResponseEntity<Void> deleteExpense(String budgetId, String expenseId) {
        expenseService.removeExpenseFromBudgetOrThrow(expenseId, budgetId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<OASExpense> findExpense(String budgetId, String expenseId) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(expenseService.findExpense(expenseId, budgetId));
    }

    @Override
    public ResponseEntity<OASExpense> modifyExpense(String budgetId, String expenseId, OASExpenseModification expenseModification) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(expenseService.updateExpense(expenseId, budgetId, expenseModification));
    }

    @Override
    public ResponseEntity<OASExpensePage> findExpenses(String budgetId, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        OASExpensePage expensePage = expenseService.findAllExpensesByBudgetId(budgetId, pageable);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(expensePage);
    }
}
