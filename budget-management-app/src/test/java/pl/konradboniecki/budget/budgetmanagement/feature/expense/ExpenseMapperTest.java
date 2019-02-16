package pl.konradboniecki.budget.budgetmanagement.feature.expense;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedExpense;
import pl.konradboniecki.budget.openapi.dto.model.OASExpense;
import pl.konradboniecki.budget.openapi.dto.model.OASExpenseCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASExpenseModification;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class ExpenseMapperTest {

    private ExpenseMapper expenseMapper;

    @BeforeAll
    public void setup() {
        expenseMapper = new ExpenseMapper();
    }

    @Nested
    class MapToExpenseTests {

        @Test
        void given_null_expenseCreation_when_map_to_expense_then_throw() {
            // Given:
            OASExpenseCreation expenseCreation = null;
            // When:
            Throwable throwable = catchThrowable(() -> expenseMapper.toExpense(expenseCreation));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_expenseCreation_when_map_to_expense_then_return_expense() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            Double amount = 5.0;
            String comment = "comment";
            OASExpenseCreation expenseCreation = new OASExpenseCreation()
                    .budgetId(budgetId)
                    .comment(comment)
                    .amount(amount);
            // When:
            Expense expense = expenseMapper.toExpense(expenseCreation);
            // Then:
            assertThat(expense).isNotNull();
            assertThat(expense.getId()).isNull();
            assertThat(expense.getCreated()).isNull();
            assertThat(expense.getBudgetId()).isEqualTo(budgetId);
            assertThat(expense.getAmount()).isEqualTo(amount);
            assertThat(expense.getComment()).isEqualTo(comment);
        }

        @Test
        void given_null_expenseModification_when_map_to_expense_then_throw() {
            // Given:
            OASExpenseModification expenseModification = null;
            // When:
            Throwable throwable = catchThrowable(() -> expenseMapper.toExpense(expenseModification));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_expenseModification_when_map_to_expense_then_return_expense() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String expenseId = UUID.randomUUID().toString();
            Double amount = 5.0;
            String comment = "comment";
            Instant created = Instant.now();
            OASExpenseModification expenseModification = new OASExpenseModification()
                    .id(expenseId)
                    .budgetId(budgetId)
                    .created(created)
                    .comment(comment)
                    .amount(amount);
            // When:
            Expense expense = expenseMapper.toExpense(expenseModification);
            // Then:
            assertThat(expense).isNotNull();
            assertThat(expense.getId()).isEqualTo(expenseId);
            assertThat(expense.getBudgetId()).isEqualTo(budgetId);
            assertThat(expense.getCreated()).isEqualTo(created);
            assertThat(expense.getComment()).isEqualTo(comment);
            assertThat(expense.getAmount()).isEqualTo(amount);
        }
    }

    @Nested
    class MapToExpenseCreationTests {

        @Test
        void given_null_expense_when_map_to_expenseCreation_then_throw() {
            // Given:
            Expense expense = null;
            // When:
            Throwable throwable = catchThrowable(() -> expenseMapper.toOASExpenseCreation(expense));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_expense_when_map_to_expenseCreation_then_return_expenseCreation() {
            // Given:
            Expense expense = populateExpense();
            // When:
            OASExpenseCreation expenseCreation = expenseMapper.toOASExpenseCreation(expense);
            // Then:
            assertThat(expenseCreation).isNotNull();
            assertThat(expenseCreation.getBudgetId()).isEqualTo(expense.getBudgetId());
            assertThat(expenseCreation.getAmount()).isEqualTo(expense.getAmount());
            assertThat(expenseCreation.getComment()).isEqualTo(expense.getComment());
        }
    }

    @Nested
    class MapToCreatedExpenseTests {
        @Test
        void given_null_expense_when_map_to_expenseCreation_then_throw() {
            // Given:
            Expense expense = null;
            // When:
            Throwable throwable = catchThrowable(() -> expenseMapper.toOASCreatedExpense(expense));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_expense_when_map_to_createdExpense_then_return_createdExpense() {
            // Given:
            Expense expense = populateExpense();
            // When:
            OASCreatedExpense createdExpense = expenseMapper.toOASCreatedExpense(expense);
            // Then:
            assertThat(createdExpense).isNotNull();
            assertThat(createdExpense.getId()).isEqualTo(expense.getId());
            assertThat(createdExpense.getBudgetId()).isEqualTo(expense.getBudgetId());
            assertThat(createdExpense.getComment()).isEqualTo(expense.getComment());
            assertThat(createdExpense.getAmount()).isEqualTo(expense.getAmount());
            assertThat(createdExpense.getCreated()).isEqualTo(expense.getCreated());
        }
    }

    @Nested
    class MapToOASExpenseTests {
        @Test
        void given_null_expense_when_map_to_oasExpense_then_throw() {
            // Given:
            Expense expense = null;
            // When:
            Throwable throwable = catchThrowable(() -> expenseMapper.toOASExpense(expense));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_expense_when_map_to_oasExpense_then_return_oasExpense() {
            // Given:
            Expense expense = populateExpense();
            // When:
            OASExpense oasExpense = expenseMapper.toOASExpense(expense);
            // Then:
            assertThat(oasExpense).isNotNull();
            assertThat(oasExpense.getId()).isEqualTo(expense.getId());
            assertThat(oasExpense.getBudgetId()).isEqualTo(expense.getBudgetId());
            assertThat(oasExpense.getCreated()).isEqualTo(expense.getCreated());
            assertThat(oasExpense.getComment()).isEqualTo(expense.getComment());
            assertThat(oasExpense.getAmount()).isEqualTo(expense.getAmount());
        }
    }

    @Nested
    class MapToExpenseModification {
        @Test
        void given_null_expense_when_map_to_expenseModification_then_throw() {
            // Given:
            Expense expense = null;
            // When:
            Throwable throwable = catchThrowable(() -> expenseMapper.toOASExpenseModification(expense));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_expense_when_map_to_expenseModification_then_return_expenseModification() {
            // Given:
            Expense expense = populateExpense();
            // When:
            OASExpenseModification expenseModification = expenseMapper.toOASExpenseModification(expense);
            // Then:
            assertThat(expenseModification).isNotNull();
            assertThat(expenseModification.getId()).isEqualTo(expense.getId());
            assertThat(expenseModification.getBudgetId()).isEqualTo(expense.getBudgetId());
            assertThat(expenseModification.getCreated()).isEqualTo(expense.getCreated());
            assertThat(expenseModification.getAmount()).isEqualTo(expense.getAmount());
            assertThat(expenseModification.getComment()).isEqualTo(expense.getComment());
        }
    }

    public Expense populateExpense() {
        return new Expense()
                .setId(UUID.randomUUID().toString())
                .setBudgetId(UUID.randomUUID().toString())
                .setAmount(5.0)
                .setComment("comment")
                .setCreated(Instant.now());
    }
}
