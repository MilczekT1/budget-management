package pl.konradboniecki.budget.budgetmanagement.feature.expense;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedExpense;
import pl.konradboniecki.budget.openapi.dto.model.OASExpense;
import pl.konradboniecki.budget.openapi.dto.model.OASExpenseCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASExpenseModification;

@Service
public class ExpenseMapper {

    public Expense toExpense(@NonNull OASExpenseCreation expenseCreation) {
        return new Expense()
                .setBudgetId(expenseCreation.getBudgetId())
                .setAmount(expenseCreation.getAmount())
                .setComment(expenseCreation.getComment());
    }

    public Expense toExpense(@NonNull OASExpenseModification expenseModification) {
        return new Expense()
                .setId(expenseModification.getId())
                .setBudgetId(expenseModification.getBudgetId())
                .setAmount(expenseModification.getAmount())
                .setComment(expenseModification.getComment())
                .setCreated(expenseModification.getCreated());
    }

    public OASExpense toOASExpense(@NonNull Expense expense) {
        return new OASExpense()
                .id(expense.getId())
                .budgetId(expense.getBudgetId())
                .amount(expense.getAmount())
                .comment(expense.getComment())
                .created(expense.getCreated());
    }

    public OASExpenseCreation toOASExpenseCreation(@NonNull Expense expense) {
        return new OASExpenseCreation()
                .budgetId(expense.getBudgetId())
                .amount(expense.getAmount())
                .comment(expense.getComment());
    }

    public OASCreatedExpense toOASCreatedExpense(@NonNull Expense expense) {
        return new OASCreatedExpense()
                .id(expense.getId())
                .budgetId(expense.getBudgetId())
                .amount(expense.getAmount())
                .comment(expense.getComment())
                .created(expense.getCreated());
    }

    public OASExpenseModification toOASExpenseModification(@NonNull Expense expense) {
        return new OASExpenseModification()
                .id(expense.getId())
                .budgetId(expense.getBudgetId())
                .amount(expense.getAmount())
                .comment(expense.getComment())
                .created(expense.getCreated());
    }
}
