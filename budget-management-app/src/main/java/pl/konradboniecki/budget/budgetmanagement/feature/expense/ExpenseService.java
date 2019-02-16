package pl.konradboniecki.budget.budgetmanagement.feature.expense;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.budgetmanagement.exception.BudgetNotFoundException;
import pl.konradboniecki.budget.budgetmanagement.exception.ExpenseCreationException;
import pl.konradboniecki.budget.budgetmanagement.exception.ExpenseNotFoundException;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.BudgetRepository;
import pl.konradboniecki.budget.openapi.dto.model.*;
import pl.konradboniecki.chassis.exceptions.BadRequestException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@AllArgsConstructor
@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    //TODO: create facade to avoid spaghetti code
    private final BudgetRepository budgetRepository;
    private final ExpenseMapper expenseMapper;

    public OASExpense findExpense(String expenseId, String budgetId) {
        Expense expense = findByIdAndBudgetIdOrThrow(expenseId, budgetId);
        return expenseMapper.toOASExpense(expense);
    }

    public Expense findByIdAndBudgetIdOrThrow(String expenseId, String budgetId) {
        budgetExistsOrThrow(budgetId, "Failed to find expense. Budget not found.");
        Optional<Expense> expense = expenseRepository.findByIdAndBudgetId(expenseId, budgetId);
        if (expense.isPresent()) {
            return expense.get();
        } else {
            throw new ExpenseNotFoundException("Expense with id: " + expenseId + " not found in budget with id: " + budgetId + ".");
        }
    }

    public OASExpensePage findAllExpensesByBudgetId(String budgetId, Pageable pageable) {
        budgetExistsOrThrow(budgetId, "Failed to list expenses. Budget not found.");

        Page<Expense> expensePage = expenseRepository.findAllByBudgetId(budgetId, pageable);
        List<OASExpense> items = expensePage.get()
                .map(expenseMapper::toOASExpense)
                .collect(Collectors.toList());

        OASPaginationMetadata paginationMetadata = new OASPaginationMetadata()
                .elements(expensePage.getNumberOfElements())
                .pageSize(pageable.getPageSize())
                .page(pageable.getPageNumber())
                .totalPages(expensePage.getTotalPages())
                .totalElements((int) expensePage.getTotalElements());
        return new OASExpensePage()
                .items(items)
                .meta(paginationMetadata);
    }

    public void removeExpenseFromBudgetOrThrow(String expenseId, String budgetId) {
        findByIdAndBudgetIdOrThrow(expenseId, budgetId);
        expenseRepository.deleteByIdAndBudgetId(expenseId, budgetId);
    }

    public OASCreatedExpense saveExpense(OASExpenseCreation expenseCreation, String budgetIdFromPath) {
        checkArgument(expenseCreation != null, "expense to save should not be null");
        checkIfBudgetIdFromPathAndBodyAreConsistent(expenseCreation, budgetIdFromPath);
        budgetExistsOrThrow(budgetIdFromPath, "Failed to create expense. Budget not found.");
        Expense expense = expenseMapper.toExpense(expenseCreation);
        expense.setId(UUID.randomUUID().toString());
        expense.setCreated(Instant.now());
        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toOASCreatedExpense(savedExpense);
    }

    private void checkIfBudgetIdFromPathAndBodyAreConsistent(OASExpenseCreation expenseCreation, String budgetIdFromPath) {
        String budgetIdFromBody = expenseCreation.getBudgetId();
        if (!budgetIdFromBody.equals(budgetIdFromPath)) {
            throw new ExpenseCreationException("Budget id in body and path don't match.");
        }
    }

    private void budgetExistsOrThrow(String budgetId, String msg) {
        if (budgetRepository.findById(budgetId).isEmpty()) {
            throw new BudgetNotFoundException(msg);
        }
    }

    public OASExpense updateExpense(String expenseId, String budgetId, OASExpenseModification expenseModification) {
        budgetIdInBodyAndPathAreConsistentOrThrow(budgetId, expenseModification);
        expenseIdInBodyAndPathAreConsistentOrThrow(expenseId, expenseModification);

        Expense origin = findByIdAndBudgetIdOrThrow(expenseId, budgetId);
        Expense modifiedExpense = expenseMapper.toExpense(expenseModification);
        Expense result = expenseRepository.save(origin.mergeWith(modifiedExpense));
        return expenseMapper.toOASExpense(result);
    }

    private void budgetIdInBodyAndPathAreConsistentOrThrow(String originId, OASExpenseModification expenseModification) {
        String budgetIdFromBody = expenseModification.getBudgetId();
        if (!budgetIdFromBody.equals(originId)) {
            throw new BadRequestException("Budget id in body and path don't match.");
        }
    }

    private void expenseIdInBodyAndPathAreConsistentOrThrow(String expenseIdFromPath, OASExpenseModification expenseModification) {
        if (!expenseIdFromPath.equals(expenseModification.getId())) {
            throw new BadRequestException("Expense id in body and path don't match.");
        }
    }
}
