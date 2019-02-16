package pl.konradboniecki.budget.budgetmanagement.feature.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseRepository extends MongoRepository<Expense, String> {

    Page<Expense> findAllByBudgetId(String budgetId, Pageable pageable);

    Optional<Expense> findByIdAndBudgetId(String id, String budgetId);

    void deleteByIdAndBudgetId(String id, String budgetId);
}
