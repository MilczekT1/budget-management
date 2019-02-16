package pl.konradboniecki.budget.budgetmanagement.feature.expense;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class ExpenseTest {

    @Test
    public void merge_populates_all_properties_if_not_null() {
        // Given:
        Expense expense = new Expense()
                .setId(UUID.randomUUID().toString())
                .setBudgetId(UUID.randomUUID().toString())
                .setComment("1")
                .setAmount(1.0)
                .setCreated(Instant.now());
        Expense expenseWithSetProperties = new Expense()
                .setId(UUID.randomUUID().toString())
                .setBudgetId(UUID.randomUUID().toString())
                .setComment("2")
                .setAmount(2.0)
                .setCreated(Instant.now());
        // When:
        Expense mergedExpense = expense.mergeWith(expenseWithSetProperties);
        // Then:
        Assertions.assertAll(
                () -> assertThat(mergedExpense.getId()).isEqualTo(expenseWithSetProperties.getId()),
                () -> assertThat(mergedExpense.getBudgetId()).isEqualTo(expenseWithSetProperties.getBudgetId()),
                () -> assertThat(mergedExpense.getComment()).isEqualTo(expenseWithSetProperties.getComment()),
                () -> assertThat(mergedExpense.getAmount()).isEqualTo(expenseWithSetProperties.getAmount()),
                () -> assertThat(mergedExpense.getCreated()).isEqualTo(expenseWithSetProperties.getCreated())
        );
    }

    @Test
    public void merge_does_not_populate_properties_if_null() {
        // Given:
        String budgetId = UUID.randomUUID().toString();
        String jarId = UUID.randomUUID().toString();
        Expense expense = new Expense()
                .setId(jarId)
                .setBudgetId(budgetId)
                .setComment("1")
                .setAmount(1.0)
                .setCreated(Instant.now());
        // When:
        Expense mergedExpense = expense.mergeWith(new Expense());
        // Then:
        Assertions.assertAll(
                () -> assertThat(mergedExpense.getId()).isEqualTo(jarId),
                () -> assertThat(mergedExpense.getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(mergedExpense.getComment()).isEqualTo("1"),
                () -> assertThat(mergedExpense.getAmount()).isEqualTo(1L),
                () -> assertThat(mergedExpense.getCreated()).isEqualTo(expense.getCreated())
        );
    }

    @Test
    public void when_merge_with_null_then_throw_npe() {
        // When:
        Throwable npe = catchThrowable(() -> new Expense().mergeWith(null));
        // Then:
        assertThat(npe).isInstanceOf(NullPointerException.class);
    }
}
