package pl.konradboniecki.budget.budgetmanagement.feature.jar;

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
import pl.konradboniecki.budget.budgetmanagement.exception.JarCreationException;
import pl.konradboniecki.budget.budgetmanagement.exception.JarNotFoundException;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.Budget;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.BudgetRepository;
import pl.konradboniecki.budget.openapi.dto.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = BudgetManagementApplication.class,
        webEnvironment = WebEnvironment.NONE,
        properties = "spring.cloud.config.enabled=false"
)
public class JarServiceTests {

    @MockBean
    private JarRepository jarRepository;
    @MockBean
    private BudgetRepository budgetRepository;
    @Autowired
    private JarService jarService;


    @Nested
    class CreationTests {
        @Test
        public void given_valid_params_when_save_then_return_jar() {
            // Given:
            String consistentBudgetId = UUID.randomUUID().toString();
            OASJarCreation jarCreation = new OASJarCreation()
                    .budgetId(consistentBudgetId)
                    .capacity(5.0)
                    .currentAmount(1.0)
                    .jarName("name");
            when(jarRepository.save(any(Jar.class)))
                    .thenReturn(new JarMapper().toJar(jarCreation));
            when(budgetRepository.findById(consistentBudgetId))
                    .thenReturn(Optional.of(new Budget().setId(consistentBudgetId)));
            // When:
            OASCreatedJar savedJar = jarService.saveJar(jarCreation, consistentBudgetId);
            // Then:
            assertThat(savedJar).isNotNull();
            assertThat(savedJar.getBudgetId()).isEqualTo(jarCreation.getBudgetId());
            assertThat(savedJar.getCapacity()).isEqualTo(jarCreation.getCapacity());
            assertThat(savedJar.getCurrentAmount()).isEqualTo(jarCreation.getCurrentAmount());
            assertThat(savedJar.getJarName()).isEqualTo(jarCreation.getJarName());
        }

        @Test
        public void given_different_budgetId_in_body_and_path_when_save_then_throw() {
            // Given:
            String budgetIdFromJar = UUID.randomUUID().toString();
            String budgetIdFromBody = UUID.randomUUID().toString();
            OASJarCreation jarCreation = new OASJarCreation()
                    .budgetId(budgetIdFromJar);
            // When:
            Throwable throwable = catchThrowable(() -> jarService.saveJar(jarCreation, budgetIdFromBody));
            // Then:
            assertThat(throwable).isInstanceOf(JarCreationException.class);
        }
    }

    @Nested
    class ModificationTests {
        @Test
        public void given_valid_params_when_update_then_return_updated_jar() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String jarId = UUID.randomUUID().toString();
            String newName = "updatedJarName";
            Jar previousJar = new Jar()
                    .setId(jarId)
                    .setBudgetId(budgetId)
                    .setJarName("initialJarName");
            when(jarRepository.findByIdAndBudgetId(jarId, budgetId))
                    .thenReturn(Optional.of(previousJar));
            Jar mockedUpdatedJar = new Jar()
                    .setId(jarId)
                    .setBudgetId(budgetId)
                    .setJarName(newName);
            when(jarRepository.save(any(Jar.class))).thenReturn(mockedUpdatedJar);
            // When:
            OASJarModification jarModification = new OASJarModification()
                    .id(jarId)
                    .budgetId(budgetId)
                    .jarName(newName);
            OASJar updatedJar = jarService.updateJar(previousJar.getId(), budgetId, jarModification);
            // Then:
            assertThat(updatedJar).isNotNull();
            assertThat(updatedJar.getId()).isEqualTo(jarId);
            assertThat(updatedJar.getBudgetId()).isEqualTo(budgetId);
            assertThat(updatedJar.getJarName()).isEqualTo(newName);
        }

        @Test
        public void given_different_budgetId_in_body_and_path_when_update_then_throw() {
            // Given:
            String budgetIdFromJar = UUID.randomUUID().toString();
            String budgetIdFromPath = UUID.randomUUID().toString();
            OASJarModification jarModification = new OASJarModification()
                    .id(UUID.randomUUID().toString())
                    .budgetId(budgetIdFromJar);
            // When:
            Throwable throwable = catchThrowable(
                    () -> jarService.updateJar(budgetIdFromJar, budgetIdFromPath, jarModification));
            // Then:
            assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class DeletionTests {
        @Test
        public void given_deleteBy_id_when_jar_found_then_no_exception() {
            // Given:
            String jarId = UUID.randomUUID().toString();
            doNothing().when(jarRepository).deleteById(jarId);
            // When:
            Throwable throwable = catchThrowable(() -> jarService.deleteJarByIdOrThrow(jarId));
            // Then:
            assertThat(throwable).isNull();
        }

        @Test
        public void given_deleteBy_id_when_jar_not_found_then_throw() {
            // Given:
            String jarId = UUID.randomUUID().toString();
            doThrow(EmptyResultDataAccessException.class)
                    .when(jarRepository)
                    .deleteById(eq(jarId));
            // When:
            Throwable throwable = catchThrowable(() -> jarService.deleteJarByIdOrThrow(jarId));
            // Then:
            assertThat(throwable).isInstanceOf(JarNotFoundException.class);
        }

        @Test
        public void given_deleteBy_idAndBudgetId_when_jar_found_then_no_exception() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String jarId = UUID.randomUUID().toString();
            when(jarRepository.deleteJarByIdAndBudgetId(jarId, budgetId))
                    .thenReturn(1L);
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            // When:
            Throwable throwable = catchThrowable(() -> jarService.removeJarFromBudgetOrThrow(jarId, budgetId));
            // Then:
            assertThat(throwable).isNull();
        }

        @Test
        public void given_deleteBy_idAndBudgetId_when_jar_not_found_then_throw() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String jarId = UUID.randomUUID().toString();
            when(jarRepository.deleteJarByIdAndBudgetId(jarId, budgetId))
                    .thenReturn(0L);
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            // When:
            Throwable throwable = catchThrowable(() -> jarService.removeJarFromBudgetOrThrow(jarId, budgetId));
            // Then:
            assertThat(throwable).isInstanceOf(JarNotFoundException.class);
        }
    }

    @Nested
    class SearchTests {
        @Test
        public void given_findAll_by_budgetId_when_jars_not_found_then_return_empty_page() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            ArrayList<Jar> jarList = new ArrayList<>();
            Pageable pageable = PageRequest.of(0, 100);
            Page<Jar> page = new PageImpl<>(jarList, pageable, 0);
            when(jarRepository.findAllByBudgetId(budgetId, pageable))
                    .thenReturn(page);
            // When:
            OASJarPage pageWithJars = jarService.findAllJarsByBudgetId(budgetId, pageable);
            // Then:
            assertThat(pageWithJars.getItems()).isEmpty();
            assertThat(pageWithJars.getMeta()).isNotNull();
            assertThat(pageWithJars.getMeta().getElements()).isEqualTo(0);
            assertThat(pageWithJars.getMeta().getTotalElements()).isEqualTo(0);
        }

        @Test
        public void given_findAll_by_budgetId_when_jars_found_then_return_page() {
            // Given:
            List<Jar> jarList = new ArrayList<>(2);
            jarList.add(new Jar().setId(UUID.randomUUID().toString()));
            jarList.add(new Jar().setId(UUID.randomUUID().toString()));
            String budgetId = UUID.randomUUID().toString();
            Pageable pageable = PageRequest.of(0, 100);
            Page<Jar> page = new PageImpl<>(jarList, pageable, 2);
            when(jarRepository.findAllByBudgetId(budgetId, pageable))
                    .thenReturn(page);
            // When:
            OASJarPage pageWithJars = jarService.findAllJarsByBudgetId(budgetId, pageable);
            // Then:
            assertThat(pageWithJars.getItems()).isNotEmpty();
            assertThat(pageWithJars.getMeta()).isNotNull();
            assertThat(pageWithJars.getMeta().getElements()).isEqualTo(2);
            assertThat(pageWithJars.getMeta().getTotalElements()).isEqualTo(2);
        }

        @Test
        public void given_findJar_when_jar_found_then_returned() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String jarId = UUID.randomUUID().toString();
            Jar mockedJar = new Jar()
                    .setId(jarId)
                    .setBudgetId(budgetId);
            when(jarRepository.findByIdAndBudgetId(jarId, budgetId))
                    .thenReturn(Optional.of(mockedJar));
            // When:
            OASJar jar = jarService.findJar(jarId, budgetId);
            // Then:
            assertThat(jar).usingRecursiveComparison()
                    .isEqualTo(new JarMapper().toOASJar(mockedJar));
        }

        @Test
        public void given_findJar_when_jar_not_found_then_throw() {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String jarId = UUID.randomUUID().toString();
            when(jarRepository.findByIdAndBudgetId(jarId, budgetId))
                    .thenReturn(Optional.empty());
            // When:
            Throwable throwable = catchThrowable(
                    () -> jarService.findJar(jarId, budgetId));
            // Then:
            assertThat(throwable).isInstanceOf(JarNotFoundException.class);
        }
    }
}
