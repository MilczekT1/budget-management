package pl.konradboniecki.budget.budgetmanagement.feature.budget;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import pl.konradboniecki.budget.openapi.dto.model.OASBudget;
import pl.konradboniecki.budget.openapi.dto.model.OASBudgetCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedBudget;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class BudgetMapperTest {

    private BudgetMapper budgetMapper;

    @BeforeAll
    public void setup() {
        budgetMapper = new BudgetMapper();
    }

    @Nested
    class MapToBudgetTests {

        @Test
        void given_null_budgetCreation_when_map_to_budget_then_throw() {
            // Given:
            OASBudgetCreation budgetCreation = null;
            // When:
            Throwable throwable = catchThrowable(() -> budgetMapper.toBudget(budgetCreation));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_budgetCreation_when_map_to_budget_then_return_budget() {
            // Given:
            String familyId = UUID.randomUUID().toString();
            Long maxJars = 5L;
            OASBudgetCreation budgetCreation = new OASBudgetCreation()
                    .familyId(familyId)
                    .maxJars(maxJars);
            // When:
            Budget budget = budgetMapper.toBudget(budgetCreation);
            // Then:
            assertThat(budget).isNotNull();
            assertThat(budget.getFamilyId()).isEqualTo(familyId);
            assertThat(budget.getMaxJars()).isEqualTo(maxJars);
        }
    }

    @Nested
    class MapToBudgetCreationTests {

        @Test
        void given_null_budget_when_map_to_budgetCreation_then_throw() {
            // Given:
            Budget budget = null;
            // When:
            Throwable throwable = catchThrowable(() -> budgetMapper.toOASBudgetCreation(budget));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_budget_when_map_to_budgetCreation_then_return_budgetCreation() {
            // Given:
            Budget budget = populateBudget();
            // When:
            OASBudgetCreation budgetCreation = budgetMapper.toOASBudgetCreation(budget);
            // Then:
            assertThat(budgetCreation).isNotNull();
            assertThat(budgetCreation.getFamilyId()).isEqualTo(budget.getFamilyId());
            assertThat(budgetCreation.getMaxJars()).isEqualTo(budget.getMaxJars());
        }
    }

    @Nested
    class MapToCreatedBudgetTests {
        @Test
        void given_null_budget_when_map_to_budgetCreation_then_throw() {
            // Given:
            Budget budget = null;
            // When:
            Throwable throwable = catchThrowable(() -> budgetMapper.toOASCreatedBudget(budget));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_budget_when_map_to_createdBudget_then_return_createdBudget() {
            // Given:
            Budget budget = populateBudget();
            // When:
            OASCreatedBudget createdBudget = budgetMapper.toOASCreatedBudget(budget);
            // Then:
            assertThat(createdBudget).isNotNull();
            assertThat(createdBudget.getId()).isEqualTo(budget.getId());
            assertThat(createdBudget.getFamilyId()).isEqualTo(budget.getFamilyId());
            assertThat(createdBudget.getMaxJars()).isEqualTo(budget.getMaxJars());
        }
    }

    @Nested
    class MapToOASBudgetTests {
        @Test
        void given_null_budget_when_map_to_oasBudget_then_throw() {
            // Given:
            Budget budget = null;
            // When:
            Throwable throwable = catchThrowable(() -> budgetMapper.toOASBudget(budget));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_budget_when_map_to_oasBudget_then_return_oasBudget() {
            // Given:
            Budget budget = populateBudget();
            // When:
            OASBudget oasBudget = budgetMapper.toOASBudget(budget);
            // Then:
            assertThat(oasBudget).isNotNull();
            assertThat(oasBudget.getId()).isEqualTo(budget.getId());
            assertThat(oasBudget.getFamilyId()).isEqualTo(budget.getFamilyId());
            assertThat(oasBudget.getMaxJars()).isEqualTo(budget.getMaxJars());
        }
    }

    public Budget populateBudget() {
        return new Budget()
                .setId(UUID.randomUUID().toString())
                .setFamilyId(UUID.randomUUID().toString())
                .setMaxJars(6L);
    }
}
