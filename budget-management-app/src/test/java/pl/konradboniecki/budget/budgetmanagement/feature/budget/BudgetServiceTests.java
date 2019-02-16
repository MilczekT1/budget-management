package pl.konradboniecki.budget.budgetmanagement.feature.budget;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.budgetmanagement.BudgetManagementApplication;
import pl.konradboniecki.budget.budgetmanagement.exception.BudgetNotFoundException;
import pl.konradboniecki.budget.budgetmanagement.exception.FamilyConflictException;
import pl.konradboniecki.budget.openapi.dto.model.OASBudget;
import pl.konradboniecki.budget.openapi.dto.model.OASBudgetCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedBudget;
import pl.konradboniecki.chassis.exceptions.BadRequestException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = BudgetManagementApplication.class,
        webEnvironment = WebEnvironment.NONE,
        properties = "spring.cloud.config.enabled=false"
)
public class BudgetServiceTests {

    @MockBean
    private BudgetRepository budgetRepository;
    @Autowired
    private BudgetService budgetService;

    @Test
    public void given_findBy_id_when_budget_not_found_then_throw() {
        // Given:
        String id = UUID.randomUUID().toString();
        when(budgetRepository.findById(id)).thenReturn(Optional.empty());
        // When:
        Throwable throwable = catchThrowable(() -> budgetService.findByOrThrow(id, "id"));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(BudgetNotFoundException.class);
    }

    @Test
    public void given_findBy_id_when_budget_found_then_return_budget() {
        // Given:
        String id = UUID.randomUUID().toString();
        ;
        Budget bgt = new Budget().setId(id);
        when(budgetRepository.findById(id)).thenReturn(Optional.of(bgt));
        // When:
        OASBudget retrievedBudget = budgetService.findByOrThrow(id, "id");
        // Then:
        assertThat(retrievedBudget).isNotNull();
        assertThat(retrievedBudget.getId()).isEqualTo(id);
    }

    @Test
    public void given_findBy_familyId_when_budget_found_then_return_budget() {
        // Given:
        String familyId = UUID.randomUUID().toString();
        Budget bgt = new Budget().setFamilyId(familyId);
        when(budgetRepository.findByFamilyId(familyId))
                .thenReturn(Optional.of(bgt));
        // When:
        OASBudget retrievedBudget = budgetService.findByOrThrow(String.valueOf(familyId), "family");
        // Then:
        assertThat(retrievedBudget).usingRecursiveComparison().isEqualTo(bgt);
    }

    @Test
    public void given_findBy_familyId_when_budget_not_found_then_throw() {
        // Given:
        String familyId = UUID.randomUUID().toString();
        when(budgetRepository.findByFamilyId(familyId))
                .thenReturn(Optional.empty());
        // When:
        Throwable throwable = catchThrowable(() -> budgetService.findByOrThrow(String.valueOf(familyId), "family"));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(BudgetNotFoundException.class);
    }

    @Test
    public void when_findBy_invalid_IdType_then_throw() {
        // Given:
        String invalidIdType = "invalidIdType";
        // When:
        Throwable throwable = catchThrowable(() -> budgetService.findByOrThrow(String.valueOf(10L), invalidIdType));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void given_save_budget_when_no_conflict_then_return_budget() {
        // Given:
        String familyIdWithoutConflict = UUID.randomUUID().toString();
        Budget budgetWithoutConflict = new Budget()
                .setId(UUID.randomUUID().toString())
                .setFamilyId(familyIdWithoutConflict)
                .setMaxJars(6L);
        OASBudgetCreation budgetCreation = new OASBudgetCreation()
                .familyId(familyIdWithoutConflict)
                .maxJars(6L);
        when(budgetRepository.save(any(Budget.class)))
                .thenReturn(budgetWithoutConflict);
        when(budgetRepository.findByFamilyId(eq(familyIdWithoutConflict)))
                .thenReturn(Optional.empty());

        // When:
        OASCreatedBudget retrievedBudget = budgetService.saveBudget(budgetCreation);
        // Then:
        assertThat(retrievedBudget).usingRecursiveComparison()
                .isEqualTo(budgetWithoutConflict);
    }

    @Test
    public void given_save_budget_when_conflict_then_throw() {
        // Given:
        String familyIdWithConflict = UUID.randomUUID().toString();
        Budget budgetWithoutConflict = new Budget()
                .setId(UUID.randomUUID().toString())
                .setFamilyId(familyIdWithConflict)
                .setMaxJars(6L);
        OASBudgetCreation budgetCreation = new OASBudgetCreation()
                .familyId(familyIdWithConflict)
                .maxJars(6L);
        when(budgetRepository.findByFamilyId(eq(familyIdWithConflict)))
                .thenReturn(Optional.of(budgetWithoutConflict));
        // When:
        Throwable throwable = catchThrowable(() -> budgetService.saveBudget(budgetCreation));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(FamilyConflictException.class);
    }
}
