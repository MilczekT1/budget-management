package pl.konradboniecki.budget.budgetmanagement.feature.jar;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedJar;
import pl.konradboniecki.budget.openapi.dto.model.OASJar;
import pl.konradboniecki.budget.openapi.dto.model.OASJarCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASJarModification;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class JarMapperTest {

    private JarMapper jarMapper;

    @BeforeAll
    public void setup() {
        jarMapper = new JarMapper();
    }

    @Nested
    class MapToJarTests {

        @Test
        void given_null_jarCreation_when_map_to_jar_then_throw() {
            // Given:
            OASJarCreation jarCreation = null;
            // When:
            Throwable throwable = catchThrowable(() -> jarMapper.toJar(jarCreation));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource({"pl.konradboniecki.budget.budgetmanagement.feature.jar.JarMapperTest#provideStatusArguments"})
        void given_jarCreation_when_map_to_jar_then_return_jar_with_valid_status(StatusArgument statusArgument) {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            Double currentAmount = statusArgument.getCurrentAmount();
            Double capacity = statusArgument.getCapacity();
            String jarName = "jarName";
            OASJarCreation jarCreation = new OASJarCreation()
                    .budgetId(budgetId)
                    .jarName(jarName)
                    .capacity(capacity)
                    .currentAmount(currentAmount);
            // When:
            Jar jar = jarMapper.toJar(jarCreation);
            // Then:
            assertThat(jar).isNotNull();
            assertThat(jar.getId()).isNull();
            assertThat(jar.getStatus()).isEqualTo(statusArgument.getExpectedStatus());
            assertThat(jar.getBudgetId()).isEqualTo(budgetId);
            assertThat(jar.getJarName()).isEqualTo(jarName);
            assertThat(jar.getCapacity()).isEqualTo(capacity);
            assertThat(jar.getCurrentAmount()).isEqualTo(currentAmount);
        }

        @Test
        void given_null_jarModification_when_map_to_jar_then_throw() {
            // Given:
            OASJarModification jarModification = null;
            // When:
            Throwable throwable = catchThrowable(() -> jarMapper.toJar(jarModification));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_expenseModification_when_map_to_expense_then_return_expense() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String jarId = UUID.randomUUID().toString();
            Double currentAmount = 5.0;
            String jarName = "comment";
            Double capacity = 100.0;
            OASJarModification jarModification = new OASJarModification()
                    .id(jarId)
                    .budgetId(budgetId)
                    .jarName(jarName)
                    .capacity(capacity)
                    .currentAmount(currentAmount)
                    .status(JarStatus.IN_PROGRESS.getStatus());
            // When:
            Jar jar = jarMapper.toJar(jarModification);
            // Then:
            assertThat(jar).isNotNull();
            assertThat(jar.getId()).isEqualTo(jarId);
            assertThat(jar.getBudgetId()).isEqualTo(budgetId);
            assertThat(jar.getCapacity()).isEqualTo(capacity);
            assertThat(jar.getJarName()).isEqualTo(jarName);
            assertThat(jar.getCurrentAmount()).isEqualTo(currentAmount);
            assertThat(jar.getStatus()).isEqualTo(JarStatus.IN_PROGRESS.getStatus());
        }
    }

    @Nested
    class MapToJarCreationTests {

        @Test
        void given_null_jar_when_map_to_jarCreation_then_throw() {
            // Given:
            Jar jar = null;
            // When:
            Throwable throwable = catchThrowable(() -> jarMapper.toOASJarCreation(jar));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_jar_when_map_to_jarCreation_then_return_jarCreation() {
            // Given:
            Jar jar = populateJar();
            // When:
            OASJarCreation jarCreation = jarMapper.toOASJarCreation(jar);
            // Then:
            assertThat(jarCreation).isNotNull();
            assertThat(jarCreation.getBudgetId()).isEqualTo(jar.getBudgetId());
            assertThat(jarCreation.getJarName()).isEqualTo(jar.getJarName());
            assertThat(jarCreation.getCapacity()).isEqualTo(jar.getCapacity());
            assertThat(jarCreation.getCurrentAmount()).isEqualTo(jar.getCurrentAmount());
        }
    }

    @Nested
    class MapToCreatedJarTests {
        @Test
        void given_null_jar_when_map_to_createdJar_then_throw() {
            // Given:
            Jar jar = null;
            // When:
            Throwable throwable = catchThrowable(() -> jarMapper.toOASCreatedJar(jar));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_jar_when_map_to_createdJar_then_return_createdJar() {
            // Given:
            Jar jar = populateJar();
            // When:
            OASCreatedJar createdJar = jarMapper.toOASCreatedJar(jar);
            // Then:
            assertThat(createdJar).isNotNull();
            assertThat(createdJar.getId()).isEqualTo(jar.getId());
            assertThat(createdJar.getBudgetId()).isEqualTo(jar.getBudgetId());
            assertThat(createdJar.getJarName()).isEqualTo(jar.getJarName());
            assertThat(createdJar.getCapacity()).isEqualTo(jar.getCapacity());
            assertThat(createdJar.getCurrentAmount()).isEqualTo(jar.getCurrentAmount());
            assertThat(createdJar.getStatus()).isEqualTo(jar.getStatus());
        }
    }

    @Nested
    class MapToOASJarTests {
        @Test
        void given_null_jar_when_map_to_oasJar_then_throw() {
            // Given:
            Jar jar = null;
            // When:
            Throwable throwable = catchThrowable(() -> jarMapper.toOASJar(jar));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_jar_when_map_to_oasJar_then_return_oasJar() {
            // Given:
            Jar jar = populateJar();
            // When:
            OASJar oasJar = jarMapper.toOASJar(jar);
            // Then:
            assertThat(oasJar).isNotNull();
            assertThat(oasJar.getId()).isEqualTo(jar.getId());
            assertThat(oasJar.getBudgetId()).isEqualTo(jar.getBudgetId());
            assertThat(oasJar.getJarName()).isEqualTo(jar.getJarName());
            assertThat(oasJar.getCapacity()).isEqualTo(jar.getCapacity());
            assertThat(oasJar.getCurrentAmount()).isEqualTo(jar.getCurrentAmount());
            assertThat(oasJar.getStatus()).isEqualTo(jar.getStatus());
        }
    }

    @Nested
    class MapToJarModification {
        @Test
        void given_null_jar_when_map_to_jarModification_then_throw() {
            // Given:
            Jar jar = null;
            // When:
            Throwable throwable = catchThrowable(() -> jarMapper.toOASJarModification(jar));
            // Then:
            assertThat(throwable).isInstanceOf(NullPointerException.class);
        }

        @Test
        void given_jar_when_map_to_jarModification_then_return_jarModification() {
            // Given:
            Jar jar = populateJar();
            // When:
            OASJarModification jarModification = jarMapper.toOASJarModification(jar);
            // Then:
            assertThat(jarModification).isNotNull();
            assertThat(jarModification.getId()).isEqualTo(jar.getId());
            assertThat(jarModification.getBudgetId()).isEqualTo(jar.getBudgetId());
            assertThat(jarModification.getJarName()).isEqualTo(jar.getJarName());
            assertThat(jarModification.getCapacity()).isEqualTo(jar.getCapacity());
            assertThat(jarModification.getCurrentAmount()).isEqualTo(jar.getCurrentAmount());
            assertThat(jarModification.getStatus()).isEqualTo(jar.getStatus());
        }
    }

    public Jar populateJar() {
        return new Jar()
                .setId(UUID.randomUUID().toString())
                .setBudgetId(UUID.randomUUID().toString())
                .setJarName("jarName")
                .setCapacity(100.0)
                .setCurrentAmount(0.0);
    }

    @Data
    @AllArgsConstructor
    static class StatusArgument {
        Double currentAmount;
        Double capacity;
        String expectedStatus;

        @Override
        public String toString() {
            return String.format("amount: %s, capacity: %s, expected status: %s", currentAmount, capacity, expectedStatus);
        }
    }

    static List<StatusArgument> provideStatusArguments() {
        return new LinkedList<>() {{
            add(new StatusArgument(0.0, 100.0, JarStatus.NOT_STARTED.getStatus()));
            add(new StatusArgument(5.0, 100.0, JarStatus.IN_PROGRESS.getStatus()));
            add(new StatusArgument(100.0, 100.0, JarStatus.COMPLETED.getStatus()));
        }};
    }
}
