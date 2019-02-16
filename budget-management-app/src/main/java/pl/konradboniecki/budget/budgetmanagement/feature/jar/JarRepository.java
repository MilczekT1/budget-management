package pl.konradboniecki.budget.budgetmanagement.feature.jar;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JarRepository extends MongoRepository<Jar, String> {

    Optional<Jar> findByIdAndBudgetId(String jarId, String budgetId);

    Page<Jar> findAllByBudgetId(String budgetId, Pageable pageable);

    Long deleteJarByIdAndBudgetId(String id, String budgetId);
}
