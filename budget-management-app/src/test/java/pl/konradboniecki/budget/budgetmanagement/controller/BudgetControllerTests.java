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
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pl.konradboniecki.budget.budgetmanagement.BudgetManagementApplication;
import pl.konradboniecki.budget.budgetmanagement.exception.FamilyConflictException;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.Budget;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.BudgetRepository;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.konradboniecki.budget.budgetmanagement.controller.BudgetController.BASE_PATH;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = BudgetManagementApplication.class,
        webEnvironment = WebEnvironment.MOCK,
        properties = "spring.cloud.config.enabled=false"
)
@AutoConfigureMockMvc
public class BudgetControllerTests {

    @Autowired
    private MockMvc mockMvc;
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
    class GET_Api_Budgets_Id {
        // GET /api/budget-mgt/v1/budgets/{budgetId}
        @Test
        public void when_budget_is_found_by_id_param_then_response_status_and_headers_are_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            Budget existingBudget = new Budget().setId(budgetId);
            when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(existingBudget));
            // Then:
            mockMvc.perform(get(BASE_PATH + "/budgets/" + budgetId + "?idType=id")
                    .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        public void when_budget_is_found_by_id_default_then_response_status_and_headers_are_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            Budget existingBudget = new Budget().setId(budgetId);
            when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(existingBudget));
            // Then:
            mockMvc.perform(get(BASE_PATH + "/budgets/" + budgetId)
                    .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        public void when_budget_is_not_found_by_id_then_response_is_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());
            // Then:
            mockMvc.perform(get(BASE_PATH + "/budgets/" + budgetId)
                    .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        public void when_budget_is_found_by_familyId_then_response_status_and_headers_are_correct() throws Exception {
            // Given:
            String familyId = UUID.randomUUID().toString();
            Budget existingBudget = new Budget().setFamilyId(familyId);
            when(budgetRepository.findByFamilyId(familyId)).thenReturn(Optional.of(existingBudget));
            // Then:
            mockMvc.perform(get(BASE_PATH + "/budgets/" + familyId + "?idType=family")
                    .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        public void when_budget_is_not_found_by_familyId_then_response_is_correct() throws Exception {
            // Given:
            String familyId = UUID.randomUUID().toString();
            when(budgetRepository.findByFamilyId(familyId)).thenReturn(Optional.empty());
            // Then:
            mockMvc.perform(get(BASE_PATH + "/budgets/" + familyId + "?idType=family")
                    .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        public void when_parameter_idType_is_invalid_then_response_is_correct() throws Exception {
            String budgetId = UUID.randomUUID().toString();
            mockMvc.perform(get(BASE_PATH + "/budgets/" + budgetId + "?idType=there_is_no_such_type")
                    .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(containsString("Invalid argument idType=there_is_no_such_type")));
        }

        @Test
        public void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            String budgetId = UUID.randomUUID().toString();
            mockMvc.perform(get(BASE_PATH + "/budgets/" + budgetId)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class POST_Api_Budgets {
        // POST /api/budget-mgt/v1/budgets
        @Test
        public void when_budget_is_created_then_response_status_and_headers_are_correct() throws Exception {
            // Given:
            Budget budgetFromBody = new Budget().setFamilyId(UUID.randomUUID().toString());
            // When:
            when(budgetRepository.save(any(Budget.class)))
                    .thenReturn(budgetFromBody.setId(UUID.randomUUID().toString()));
            // Then:
            mockMvc.perform(post("/api/budget-mgt/v1/budgets")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", basicAuthHeaderValue)
                    .content(new ObjectMapper().writeValueAsString(budgetFromBody)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        public void when_budget_is_not_created_then_response_is_correct() throws Exception {
            // Given:
            Budget budgetFromBody = new Budget().setFamilyId(UUID.randomUUID().toString());
            // When:
            doThrow(FamilyConflictException.class).when(budgetRepository).save(any(Budget.class));
            // Then:
            mockMvc.perform(post("/api/budget-mgt/v1/budgets")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", basicAuthHeaderValue)
                    .content(new ObjectMapper().writeValueAsString(budgetFromBody)))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(containsString("Unexpected error occurred.")));
        }

        @Test
        public void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            mockMvc.perform(post("/api/budget-mgt/v1/budgets")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DELETE_Api_Budgets_Id {
        // POST /api/budget-mgt/v1/budgets/{{budgetId}}
        @Test
        public void when_budget_is_deleted_then_response_status_and_headers_are_correct() throws Exception {
            // Given:
            String uuid = UUID.randomUUID().toString();
            Budget bgt = new Budget().setId(uuid);
            // When:
            when(budgetRepository.findById(any(String.class))).thenReturn(Optional.of(bgt));
            doNothing().when(budgetRepository).deleteById(any(String.class));
            // Then:
            mockMvc.perform(delete(BASE_PATH + "/budgets/" + uuid)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", basicAuthHeaderValue))
                    .andExpect(status().isNoContent());
        }

        @Test
        public void when_budget_is_not_found_during_deletion_then_response_status_and_headers_are_correct() throws Exception {
            // Given:
            String uuid = UUID.randomUUID().toString();
            // When:
            when(budgetRepository.findById(eq(uuid))).thenReturn(Optional.empty());
            // Then:
            mockMvc.perform(delete(BASE_PATH + "/budgets/" + uuid)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", basicAuthHeaderValue))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }
}
