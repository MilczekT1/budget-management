package pl.konradboniecki.budget.budgetmanagement.feature.budget;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends MongoRepository<Budget, String> {
    Optional<Budget> findByFamilyId(String id);

    void deleteById(String aLong);
}
