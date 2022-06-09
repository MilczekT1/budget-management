package pl.konradboniecki.budget.budgetmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pl.konradboniecki.budget.budgetmanagement.BudgetManagementApplication;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.Budget;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.BudgetRepository;
import pl.konradboniecki.budget.budgetmanagement.feature.jar.Jar;
import pl.konradboniecki.budget.budgetmanagement.feature.jar.JarMapper;
import pl.konradboniecki.budget.budgetmanagement.feature.jar.JarRepository;
import pl.konradboniecki.budget.openapi.dto.model.OASJarCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASJarModification;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = BudgetManagementApplication.class,
        webEnvironment = WebEnvironment.MOCK,
        properties = "spring.cloud.config.enabled=false"
)
@AutoConfigureMockMvc
class JarControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JarMapper jarMapper;
    @MockBean
    private JarRepository jarRepository;
    @MockBean
    private BudgetRepository budgetRepository;
    @Autowired
    private ChassisSecurityBasicAuthHelper chassisSecurityBasicAuthHelper;

    private String basicAuthHeaderValue;

    @BeforeAll
    public void createBasicAuthHeader() {
        basicAuthHeaderValue = chassisSecurityBasicAuthHelper.getBasicAuthHeaderValue();
    }

    @Nested
    class GET_Api_Budgets_Id_Jars {
        // GET /api/budget-mgt/v1/budgets/{budgetId}/jars
        @Test
        void when_jars_are_found_then_response_status_and_headers_are_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            Jar firstJar = new Jar()
                    .setJarName("name1")
                    .setBudgetId(budgetId)
                    .setId(UUID.randomUUID().toString())
                    .setCurrentAmount(0.0)
                    .setCapacity(3.0);
            Jar secondJar = new Jar()
                    .setJarName("name2")
                    .setBudgetId(budgetId)
                    .setId(UUID.randomUUID().toString())
                    .setCurrentAmount(0.0)
                    .setCapacity(3.0);
            ArrayList<Jar> jarList = new ArrayList<>();
            jarList.add(firstJar);
            jarList.add(secondJar);
            Pageable pageable = PageRequest.of(0, 100);
            Page<Jar> page = new PageImpl<>(jarList, pageable, 0);
            when(jarRepository.findAllByBudgetId(budgetId, pageable))
                    .thenReturn(page);
            // Then:
            mockMvc.perform(get("/api/budget-mgt/v1/budgets/{budgetId}/jars", budgetId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            String randomBudgetId = UUID.randomUUID().toString();
            mockMvc.perform(get("/api/budget-mgt/v1/budgets/{budgetId}/jars", randomBudgetId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GET_Api_Budgets_Id_Jars_Id {
        // GET /api/budget-mgt/v1/budgets/{budgetId}/jars/{id}
        @Test
        void when_jar_not_found_then_response_is_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String jarId = UUID.randomUUID().toString();
            when(jarRepository.findByIdAndBudgetId(jarId, budgetId))
                    .thenReturn(Optional.empty());
            // Then:
            mockMvc.perform(get("/api/budget-mgt/v1/budgets/{budgetId}/jars/{jarId}", budgetId, jarId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(containsString("Jar with id: " + jarId + " not found in budget with id: " + budgetId)));

        }

        @Test
        void when_jar_is_found_then_response_status_and_headers_are_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String jarId = UUID.randomUUID().toString();
            Jar mockedJar = new Jar()
                    .setCapacity(1.0)
                    .setCurrentAmount(1.0)
                    .setId(jarId)
                    .setJarName("testName")
                    .setBudgetId(budgetId);
            when(jarRepository.findByIdAndBudgetId(jarId, budgetId))
                    .thenReturn(Optional.of(mockedJar));
            // Then:
            mockMvc.perform(get("/api/budget-mgt/v1/budgets/{budgetId}/jars/{jarId}", budgetId, jarId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        }

        @Test
        void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            String randomBudgetId = UUID.randomUUID().toString();
            mockMvc.perform(get("/api/budget-mgt/v1/budgets/{budgetId}/jars/1", randomBudgetId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class POST_Api_Budgets_Id_Jars {
        // POST /api/budget-mgt/v1/budgets/{budgetId}/jars
        @Test
        void when_jar_is_created_then_response_status_and_headers_are_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            OASJarCreation jarCreation = new OASJarCreation()
                    .jarName("name")
                    .budgetId(budgetId)
                    .capacity(5.0)
                    .currentAmount(1.0);
            Jar jar = jarMapper.toJar(jarCreation)
                    .setId(UUID.randomUUID().toString());
            when(jarRepository.save(any(Jar.class)))
                    .thenReturn(jar);
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            // Then:
            mockMvc.perform(post("/api/budget-mgt/v1/budgets/{budgetId}/jars", budgetId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", basicAuthHeaderValue)
                            .content(new ObjectMapper().writeValueAsString(jarCreation)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void when_budgetId_does_not_match_then_response_status_and_headers_are_ok() throws Exception {
            // Given:
            String budgetIdFromBody = UUID.randomUUID().toString();
            String budgetIdFromPath = UUID.randomUUID().toString();
            OASJarCreation jarCreation = new OASJarCreation()
                    .jarName("name")
                    .budgetId(budgetIdFromBody)
                    .capacity(5.0)
                    .currentAmount(1.0);

            // Then:
            mockMvc.perform(post("/api/budget-mgt/v1/budgets/{budgetId}/jars", budgetIdFromPath)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", basicAuthHeaderValue)
                            .content(new ObjectMapper().writeValueAsString(jarCreation)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(containsString("Budget id in body and path don't match.")));
        }

        @Test
        void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            String randomBudgetId = UUID.randomUUID().toString();
            mockMvc.perform(post("/api/budget-mgt/v1/budgets/{budgetId}/jars", randomBudgetId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class PUT_Api_Budgets_Id_Jars_Id {
        // PUT /api/budget-mgt/v1/budgets/{budgetId}/jars/{jarId}
        @Test
        void when_jar_is_updated_then_response_is_ok() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String jarId = UUID.randomUUID().toString();
            Jar originJar = new Jar()
                    .setId(jarId)
                    .setJarName("name")
                    .setBudgetId(budgetId)
                    .setCapacity(4.0)
                    .setCurrentAmount(3.0);
            OASJarModification jarModification = new OASJarModification()
                    .id(jarId)
                    .jarName("modifiedName")
                    .budgetId(budgetId)
                    .capacity(5.0)
                    .currentAmount(4.0);
            Jar jarInRequestBody = jarMapper.toJar(jarModification);

            // When:
            Jar mergedJar = originJar.mergeWith(jarInRequestBody);
            when(jarRepository.findByIdAndBudgetId(jarId, budgetId)).thenReturn(Optional.of(originJar));
            when(jarRepository.save(any(Jar.class))).thenReturn(mergedJar);

            // Then:
            mockMvc.perform(
                            put("/api/budget-mgt/v1/budgets/{budgetId}/jars/{jarId}", budgetId, jarId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", basicAuthHeaderValue)
                            .content(new ObjectMapper().writeValueAsString(jarModification)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void when_jar_is_not_found_during_update_then_response_is_ok() throws Exception {
            // Given:
            String jarId = UUID.randomUUID().toString();
            String budgetId = UUID.randomUUID().toString();
            Jar jarInRequestBody = new Jar()
                    .setId(jarId)
                    .setJarName("modifiedName")
                    .setBudgetId(budgetId)
                    .setCapacity(5.0)
                    .setCurrentAmount(4.0);
            // When:
            when(jarRepository.findByIdAndBudgetId(jarId, budgetId)).thenReturn(Optional.empty());
            // Then:
            mockMvc.perform(put("/api/budget-mgt/v1/budgets/{budgetId}/jars/{jarId}", budgetId, jarId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", basicAuthHeaderValue)
                            .content(new ObjectMapper().writeValueAsString(jarInRequestBody)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            String randomBudgetId = UUID.randomUUID().toString();
            mockMvc.perform(put("/api/budget-mgt/v1/budgets/{budgetId}/jars/1", randomBudgetId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DELETE_Api_Budgets_Id_Jars_Id {
        // DELETE /api/budget-mgt/v1/budgets/{budgetId}/jars/{id}
        @Test
        void when_jar_found_during_deletion_then_response_is_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String jarId = UUID.randomUUID().toString();
            when(jarRepository.deleteJarByIdAndBudgetId(jarId, budgetId))
                    .thenReturn(1L);
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));

            // Then:
            mockMvc.perform(
                            delete("/api/budget-mgt/v1/budgets/{budgetId}/jars/{jarId}", budgetId, jarId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andExpect(status().isNoContent());
        }

        @Test
        void when_jar_not_found_during_deletion_then_response_is_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String jarId = UUID.randomUUID().toString();
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            when(jarRepository.deleteJarByIdAndBudgetId(jarId, budgetId))
                    .thenReturn(0L);

            // Then:
            mockMvc.perform(
                            delete("/api/budget-mgt/v1/budgets/{budgetId}/jars/{jarId}", budgetId, jarId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(containsString("Jar with id: " + jarId + " not found in budget with id: " + budgetId)));
        }

        @Test
        void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            String randomBudgetId = UUID.randomUUID().toString();
            mockMvc.perform(delete("/api/budget-mgt/v1/budgets/{budgetId}/jars/1", randomBudgetId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
