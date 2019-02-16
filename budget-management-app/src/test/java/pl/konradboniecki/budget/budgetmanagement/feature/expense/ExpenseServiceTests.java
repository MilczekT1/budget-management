package pl.konradboniecki.budget.budgetmanagement.feature.expense;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.budgetmanagement.BudgetManagementApplication;
import pl.konradboniecki.budget.budgetmanagement.exception.BudgetNotFoundException;
import pl.konradboniecki.budget.budgetmanagement.exception.ExpenseCreationException;
import pl.konradboniecki.budget.budgetmanagement.exception.ExpenseNotFoundException;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.Budget;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.BudgetRepository;
import pl.konradboniecki.budget.openapi.dto.model.*;
import pl.konradboniecki.chassis.exceptions.BadRequestException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = BudgetManagementApplication.class,
        webEnvironment = WebEnvironment.NONE,
        properties = "spring.cloud.config.enabled=false"
)
public class ExpenseServiceTests {

    @MockBean
    private ExpenseRepository expenseRepository;
    @MockBean
    private BudgetRepository budgetRepository;
    @Autowired
    private ExpenseService expenseService;

    @Nested
    class DeletionTests {
        @Test
        public void given_deleteBy_idAndBudgetId_when_expense_found_then_do_nothing() {
            // Given:
            String randomBudgetId = UUID.randomUUID().toString();
            String expenseId = UUID.randomUUID().toString();
            when(budgetRepository.findById(randomBudgetId))
                    .thenReturn(Optional.of(new Budget().setId(randomBudgetId)));
            doNothing().when(expenseRepository).deleteByIdAndBudgetId(expenseId, randomBudgetId);
            when(expenseRepository.findByIdAndBudgetId(expenseId, randomBudgetId))
                    .thenReturn(Optional.of(new Expense()));
            // When:
            Throwable throwable = catchThrowable(
                    () -> expenseService.removeExpenseFromBudgetOrThrow(expenseId, randomBudgetId));
            // Then:
            assertThat(throwable).isNull();
        }

        @Test
        public void given_deleteBy_idAndBudgetId_when_expense_not_found_then_throw() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String expenseId = UUID.randomUUID().toString();
            doThrow(EmptyResultDataAccessException.class)
                    .when(expenseRepository)
                    .deleteByIdAndBudgetId(expenseId, budgetId);
            // When:
            Throwable throwable = catchThrowable(() -> expenseService.removeExpenseFromBudgetOrThrow(expenseId, budgetId));
            // Then:
            assertThat(throwable).isInstanceOf(BudgetNotFoundException.class);
        }
    }

    @Nested
    class ModificationTests {
        @Test
        public void given_different_budgetId_in_body_and_path_when_update_then_throw() {
            // Given:
            String budgetIdFromExpense = UUID.randomUUID().toString();
            String budgetIdFromPath = UUID.randomUUID().toString();
            Expense expenseFromBody = new Expense()
                    .setId(UUID.randomUUID().toString())
                    .setBudgetId(budgetIdFromExpense);
            OASExpenseModification expenseModification = new OASExpenseModification()
                    .id(UUID.randomUUID().toString())
                    .budgetId(budgetIdFromExpense);
            // When:
            Throwable throwable = catchThrowable(
                    () -> expenseService.updateExpense(expenseFromBody.getId(), budgetIdFromPath, expenseModification));
            // Then:
            assertThat(throwable).isInstanceOf(BadRequestException.class);
        }

        @Test
        public void given_the_same_budgetId_in_body_and_path_when_update_then_return_expense() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String expenseId = UUID.randomUUID().toString();
            Expense expenseFromBody = new Expense()
                    .setId(expenseId)
                    .setBudgetId(budgetId);
            OASExpenseModification expenseModification = new OASExpenseModification()
                    .id(expenseId)
                    .budgetId(budgetId);
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            when(expenseRepository.findByIdAndBudgetId(expenseId, budgetId))
                    .thenReturn(Optional.of(expenseFromBody));
            when(expenseRepository.save(any(Expense.class))).thenReturn(expenseFromBody);
            // When:
            OASExpense updatedExpense = expenseService.updateExpense(expenseFromBody.getId(), budgetId, expenseModification);
            // Then:
            assertThat(updatedExpense).isNotNull();
        }
    }

    @Nested
    class CreationTests {
        @Test
        public void given_valid_params_when_save_then_return_expense() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            OASExpenseCreation expenseCreation = new OASExpenseCreation()
                    .budgetId(budgetId);

            when(expenseRepository.save(any(Expense.class))).thenReturn(new Expense());
            when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(new Budget()));
            // When:
            OASCreatedExpense createdExpense = expenseService.saveExpense(expenseCreation, budgetId);
            // Then:
            assertThat(createdExpense).isNotNull();
        }

        @Test
        public void given_missing_body_when_save_then_throw() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            when(expenseRepository.save(any(Expense.class))).thenReturn(new Expense());
            when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());
            // When:
            Throwable throwable = catchThrowable(() ->
                    expenseService.saveExpense(null, budgetId));
            // Then:
            assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        public void given_different_budgetId_in_body_and_path_when_save_then_throw() {
            // Given:
            String budgetIdFromBody = UUID.randomUUID().toString();
            String budgetIdFromPath = UUID.randomUUID().toString();
            OASExpenseCreation expenseCreation = new OASExpenseCreation()
                    .budgetId(budgetIdFromBody);
            // When:
            Throwable throwable = catchThrowable(() -> expenseService.saveExpense(expenseCreation, budgetIdFromPath));
            // Then:
            assertThat(throwable).isInstanceOf(ExpenseCreationException.class);
        }
    }

    @Nested
    class SearchTests {
        @Test
        public void given_findBy_idAndBudgetId_when_expense_found_then_returned() {
            // Given:
            String randomUUID = UUID.randomUUID().toString();
            String expenseId = UUID.randomUUID().toString();
            Expense mockedExpense = new Expense()
                    .setAmount(5D)
                    .setBudgetId(randomUUID)
                    .setId(expenseId)
                    .setComment("testComment");
            when(budgetRepository.findById(randomUUID))
                    .thenReturn(Optional.of(new Budget().setId(randomUUID)));
            when(expenseRepository.findByIdAndBudgetId(expenseId, randomUUID))
                    .thenReturn(Optional.of(mockedExpense));
            // When:
            Expense expense = expenseService.findByIdAndBudgetIdOrThrow(expenseId, randomUUID);
            // Then:
            assertThat(expense).usingRecursiveComparison().isEqualTo(mockedExpense);
        }

        @Test
        public void given_findBy_idAndBudgetId_when_expense_not_found_then_throw() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String expenseId = UUID.randomUUID().toString();
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            when(expenseRepository.findByIdAndBudgetId(expenseId, budgetId))
                    .thenReturn(Optional.empty());
            // When:
            Throwable throwable = catchThrowable(
                    () -> expenseService.findByIdAndBudgetIdOrThrow(expenseId, budgetId));
            // Then:
            assertThat(throwable).isInstanceOf(ExpenseNotFoundException.class);
        }

        @Test
        public void given_findBy_idAndBudgetId_when_budget_not_found_then_throw() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.empty());
            // When:
            Throwable throwable = catchThrowable(
                    () -> expenseService.findByIdAndBudgetIdOrThrow(UUID.randomUUID().toString(), budgetId));
            // Then:
            assertThat(throwable).isInstanceOf(BudgetNotFoundException.class);
        }

        @Test
        public void given_findAll_by_budgetId_when_expenses_not_found_then_return_empty_items() {
            // Given:
            ArrayList<Expense> expenseList = new ArrayList<>();
            Pageable pageable = PageRequest.of(0, 100);
            Page<Expense> page = new PageImpl<>(expenseList, pageable, 0);
            String randomBudgetId = UUID.randomUUID().toString();
            when(budgetRepository.findById(randomBudgetId))
                    .thenReturn(Optional.of(new Budget().setId(randomBudgetId)));
            when(expenseRepository.findAllByBudgetId(eq(randomBudgetId), eq(pageable)))
                    .thenReturn(page);
            // When:
            OASExpensePage pageWithExpenses = expenseService.findAllExpensesByBudgetId(randomBudgetId, pageable);
            // Then:
            assertThat(pageWithExpenses.getItems()).isEmpty();
        }

        @Test
        public void given_findAll_by_budgetId_when_budget_not_found_then_throw() {
            // Given:
            ArrayList<Expense> expenseList = new ArrayList<>();
            Pageable pageable = PageRequest.of(0, 100);
            Page<Expense> page = new PageImpl<>(expenseList, pageable, 0);
            String randomBudgetId = UUID.randomUUID().toString();
            when(budgetRepository.findById(randomBudgetId))
                    .thenReturn(Optional.empty());
            when(expenseRepository.findAllByBudgetId(eq(randomBudgetId), eq(pageable)))
                    .thenReturn(page);
            // When:
            Throwable throwable = catchThrowable(() -> expenseService.findAllExpensesByBudgetId(randomBudgetId, pageable));
            // Then:
            assertThat(throwable).isInstanceOf(BudgetNotFoundException.class);
        }

        @Test
        public void given_findAll_by_budgetId_when_expenses_found_then_return_items() {
            // Given:
            String randomBudgetId = UUID.randomUUID().toString();
            ArrayList<Expense> expenseList = new ArrayList<>();
            expenseList.add(new Expense().setBudgetId(randomBudgetId));
            expenseList.add(new Expense().setBudgetId(randomBudgetId));
            Pageable pageable = PageRequest.of(0, 100);
            Page<Expense> page = new PageImpl<>(expenseList, pageable, 0);
            when(budgetRepository.findById(randomBudgetId))
                    .thenReturn(Optional.of(new Budget().setId(randomBudgetId)));
            when(expenseRepository.findAllByBudgetId(eq(randomBudgetId), eq(pageable)))
                    .thenReturn(page);
            // When:
            OASExpensePage pageWithExpenses = expenseService.findAllExpensesByBudgetId(randomBudgetId, pageable);
            // Then:
            assertThat(pageWithExpenses.getItems().size()).isEqualTo(2L);
            pageWithExpenses.getItems().forEach((expense) -> assertThat(expense.getBudgetId()).isEqualTo(randomBudgetId));
        }
    }
}
