package pl.konradboniecki.budget.budgetmanagement.feature.jar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@TestInstance(Lifecycle.PER_CLASS)
public class JarTest {

    @Test
    public void set_amount_greater_than_capacity_should_set_status_completed() {
        // Given:
        Jar jar = new Jar().setCapacity(10.0);
        // When:
        jar.setCurrentAmount(50.0);
        // Then:
        assertThat(jar.getStatus()).isEqualTo(JarStatus.COMPLETED.getStatus());
    }

    @Test
    public void set_amount_equal_to_capacity_should_set_status_completed() {
        // Given:
        Jar jar = new Jar().setCapacity(10.0);
        // When:
        jar.setCurrentAmount(jar.getCapacity());
        // Then:
        assertThat(jar.getStatus()).isEqualTo(JarStatus.COMPLETED.getStatus());
    }

    @Test
    public void set_amount_lower_than_capacity_should_set_status_IN_PROGRESS() {
        // Given:
        Jar jar = new Jar().setCapacity(10.0);
        // When:
        jar.setCurrentAmount(1.0);
        // Then:
        assertThat(jar.getStatus()).isEqualTo(JarStatus.IN_PROGRESS.getStatus());
    }

    @Test
    public void default_status_is_NOT_STARTED() {
        // When:
        Jar jar = new Jar();
        // Then:
        assertThat(jar.getStatus()).isEqualTo(JarStatus.NOT_STARTED.getStatus());
    }

    @Test
    public void merge_populates_all_properties_if_not_null() {
        // Given:
        Jar jar = new Jar()
                .setId(UUID.randomUUID().toString())
                .setBudgetId(UUID.randomUUID().toString())
                .setCapacity(2.0)
                .setJarName("nameBeforeMerge")
                .setCurrentAmount(1.0);
        Jar jarWithSetProperties = new Jar()
                .setId(UUID.randomUUID().toString())
                .setBudgetId(UUID.randomUUID().toString())
                .setCapacity(3.0)
                .setCurrentAmount(3.0)
                .setJarName("nameAfterMerge");
        // When:
        Jar mergedJar = jar.mergeWith(jarWithSetProperties);
        // Then:
        Assertions.assertAll(
                () -> assertThat(mergedJar.getId()).isEqualTo(jarWithSetProperties.getId()),
                () -> assertThat(mergedJar.getBudgetId()).isEqualTo(jarWithSetProperties.getBudgetId()),
                () -> assertThat(mergedJar.getCapacity()).isEqualTo(jarWithSetProperties.getCapacity()),
                () -> assertThat(mergedJar.getCurrentAmount()).isEqualTo(jarWithSetProperties.getCurrentAmount()),
                () -> assertThat(mergedJar.getJarName()).isEqualTo(jarWithSetProperties.getJarName()),
                () -> assertThat(mergedJar.getStatus()).isEqualTo(JarStatus.COMPLETED.getStatus())
        );
    }

    @Test
    public void merge_does_not_populate_properties_if_null() {
        // Given:
        Jar jar = new Jar()
                .setId(UUID.randomUUID().toString())
                .setBudgetId(UUID.randomUUID().toString())
                .setCapacity(2.0)
                .setJarName("nameBeforeMerge")
                .setCurrentAmount(1.0);
        Jar jarWithSetProperties = new Jar()
                .setId(null)
                .setBudgetId(null)
                .setCurrentAmount(null)
                .setCapacity(null)
                .setJarName(null);
        // When:
        Jar mergedJar = jar.mergeWith(jarWithSetProperties);
        // Then:
        Assertions.assertAll(
                () -> assertThat(mergedJar.getId()).isEqualTo(jar.getId()),
                () -> assertThat(mergedJar.getBudgetId()).isEqualTo(jar.getBudgetId()),
                () -> assertThat(mergedJar.getCapacity()).isEqualTo(jar.getCapacity()),
                () -> assertThat(mergedJar.getCurrentAmount()).isEqualTo(jar.getCurrentAmount()),
                () -> assertThat(mergedJar.getJarName()).isEqualTo(jar.getJarName()),
                () -> assertThat(mergedJar.getStatus()).isEqualTo(JarStatus.IN_PROGRESS.getStatus())
        );
    }

    @Test
    public void merge_throws_NPE_if_jar_is_null() {
        // Given:
        Jar firstJar = new Jar();
        // When:
        Throwable throwable = catchThrowable(() -> firstJar.mergeWith(null));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void setting_currentAmount_changes_status() {
        // Given:
        Jar firstJar = new Jar()
                .setCapacity(5.0)
                .setCurrentAmount(6.0);
        assertThat(firstJar.getStatus()).isEqualTo(JarStatus.COMPLETED.getStatus());
        // When:
        firstJar.setCurrentAmount(3.0);
        // Then:
        assertThat(firstJar.getStatus()).isEqualTo(JarStatus.IN_PROGRESS.getStatus());
    }

    @Test
    public void setting_capacity_changes_status() {
        // Given:
        Jar firstJar = new Jar()
                .setCurrentAmount(6.0)
                .setCapacity(5.0);
        assertThat(firstJar.getStatus()).isEqualTo(JarStatus.COMPLETED.getStatus());
        // When:
        firstJar.setCapacity(7.0);
        // Then:
        assertThat(firstJar.getStatus()).isEqualTo(JarStatus.IN_PROGRESS.getStatus());
    }

    @Test
    public void setting_capacity_to_negative_changes_status_to_NOT_STARTED() {
        // Given:
        Jar firstJar = new Jar()
                .setCurrentAmount(6.0)
                .setCapacity(5.0);
        Jar secondJar = new Jar()
                .setCurrentAmount(6.0)
                .setCapacity(5.0);
        assertThat(firstJar.getStatus()).isEqualTo(JarStatus.COMPLETED.getStatus());
        // When:
        firstJar.setCapacity(0.0);
        secondJar.setCapacity(-1.0);
        // Then:
        assertThat(firstJar.getStatus()).isEqualTo(JarStatus.NOT_STARTED.getStatus());
        assertThat(secondJar.getStatus()).isEqualTo(JarStatus.NOT_STARTED.getStatus());
    }

    @Test
    public void when_capacity_gt_currentAmount_then_status_is_IN_PROGRESS() {
        // Given:
        Jar jar = new Jar();
        // When:
        jar.setCurrentAmount(2.0);
        jar.setCapacity(5.0);
        // Then:
        assertThat(jar.getStatus()).isEqualTo(JarStatus.IN_PROGRESS.getStatus());
    }

    @Test
    public void when_capacity_lt_currentAmount_then_status_is_COMPLETED() {
        // Given:
        Jar jar = new Jar();
        // When:
        jar.setCurrentAmount(6.0);
        jar.setCapacity(5.0);
        // Then:
        assertThat(jar.getStatus()).isEqualTo(JarStatus.COMPLETED.getStatus());
    }

    @Test
    public void when_capacity_is_null_then_status_is_NOT_STARTED() {
        // Given:
        Jar jar = new Jar();
        // When:
        jar.setCurrentAmount(6.0);
        jar.setCapacity(null);
        // Then:
        assertThat(jar.getStatus()).isEqualTo(JarStatus.NOT_STARTED.getStatus());
    }

    @Test
    public void when_currentAmount_is_null_then_status_is_NOT_STARTED() {
        // Given:
        Jar jar = new Jar();
        // When:
        jar.setCurrentAmount(null);
        jar.setCapacity(5.0);
        // Then:
        assertThat(jar.getStatus()).isEqualTo(JarStatus.NOT_STARTED.getStatus());
    }

    @Test
    public void when_capacity_is_lt_zero_then_status_is_NOT_STARTED() {
        // Given:
        Jar jar = new Jar();
        // When:
        jar.setCurrentAmount(1.0);
        jar.setCapacity(-1.0);
        // Then:
        assertThat(jar.getStatus()).isEqualTo(JarStatus.NOT_STARTED.getStatus());
    }

    @Test
    public void when_capacity_is_equal_to_zero_then_status_is_NOT_STARTED() {
        // Given:
        Jar jar = new Jar();
        // When:
        jar.setCurrentAmount(1.0);
        jar.setCapacity(0.0);
        // Then:
        assertThat(jar.getStatus()).isEqualTo(JarStatus.NOT_STARTED.getStatus());
    }
}
