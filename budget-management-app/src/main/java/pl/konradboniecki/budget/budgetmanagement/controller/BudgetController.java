package pl.konradboniecki.budget.budgetmanagement.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.BudgetService;
import pl.konradboniecki.budget.openapi.api.BudgetManagementApi;
import pl.konradboniecki.budget.openapi.dto.model.OASBudget;
import pl.konradboniecki.budget.openapi.dto.model.OASBudgetCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedBudget;

import static pl.konradboniecki.budget.budgetmanagement.controller.BudgetController.BASE_PATH;

@AllArgsConstructor
@RestController
@RequestMapping(value = BASE_PATH)
public class BudgetController implements BudgetManagementApi {
    public static final String BASE_PATH = "/api/budget-mgt/v1";

    private final BudgetService budgetService;

    @Override
    public ResponseEntity<OASCreatedBudget> createBudget(OASBudgetCreation budgetCreation) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(budgetService.saveBudget(budgetCreation));
    }

    @Override
    public ResponseEntity<OASBudget> findBudget(String budgetId, String idType) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(budgetService.findByOrThrow(budgetId, idType));
    }

    @Override
    public ResponseEntity<Void> deleteBudget(String budgetId) {
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.noContent().build();
    }
}
