package pl.konradboniecki.budget.budgetmanagement.feature.budget;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class BudgetTests {

    @Test
    void default_maxJars_is_equal_to_6() {
        // When:
        Budget bgt = new Budget();
        // Then:
        assertThat(bgt.getMaxJars()).isEqualTo(6L);
    }
}
