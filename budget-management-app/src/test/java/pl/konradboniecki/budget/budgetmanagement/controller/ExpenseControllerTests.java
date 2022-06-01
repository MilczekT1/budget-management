package pl.konradboniecki.budget.budgetmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.konradboniecki.budget.budgetmanagement.BudgetManagementApplication;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.Budget;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.BudgetRepository;
import pl.konradboniecki.budget.budgetmanagement.feature.expense.Expense;
import pl.konradboniecki.budget.budgetmanagement.feature.expense.ExpenseMapper;
import pl.konradboniecki.budget.budgetmanagement.feature.expense.ExpenseRepository;
import pl.konradboniecki.budget.openapi.dto.model.OASExpenseCreation;
import pl.konradboniecki.chassis.exceptions.ErrorDescription;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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
class ExpenseControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ExpenseMapper expenseMapper;
    @MockBean
    private ExpenseRepository expenseRepository;
    @MockBean
    private BudgetRepository budgetRepository;
    @Autowired
    private ChassisSecurityBasicAuthHelper chassisSecurityBasicAuthHelper;

    private String basicAuthHeaderValue;

    @BeforeAll
    void createBasicAuthHeader() {
        basicAuthHeaderValue = chassisSecurityBasicAuthHelper.getBasicAuthHeaderValue();
    }

    @Nested
    class GET_Api_Budgets_Id_Expenses_Id {
        // GET /api/budget-mgt/v1/budgets/{id}/expenses/{expenseId}
        @Test
        void when_expense_not_found_then_response_is_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String expenseId = UUID.randomUUID().toString();
            when(expenseRepository.findByIdAndBudgetId(expenseId, budgetId))
                    .thenReturn(Optional.empty());
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            // Then:
            mockMvc.perform(
                            get("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", budgetId, expenseId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(containsString("Expense with id: " + expenseId + " not found in budget with id: " + budgetId + ".")));

        }

        @Test
        void when_expense_found_then_response_status_and_headers_are_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String expenseId = UUID.randomUUID().toString();
            Expense mockedExpense = new Expense()
                    .setAmount(5.0)
                    .setBudgetId(budgetId)
                    .setId(expenseId)
                    .setComment("testComment");
            when(expenseRepository.findByIdAndBudgetId(expenseId, budgetId))
                    .thenReturn(Optional.of(mockedExpense));
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            // Then:
            mockMvc.perform(
                            get("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", budgetId, expenseId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json("{\"id\":" + expenseId + ",\"budgetId\":" + budgetId + ",\"amount\":5,\"comment\":\"testComment\",\"created\":null}"));

        }

        @Test
        void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            String randomUUID = UUID.randomUUID().toString();
            mockMvc.perform(get("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", randomUUID, randomUUID))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GET_Api_Budgets_Id_Expenses {
        // GET /api/budget-mgt/v1/budgets/{id}/expenses
        @Test
        void when_expenses_found_with_default_params_then_response_is_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            Expense firstExpense = new Expense()
                    .setId(UUID.randomUUID().toString())
                    .setBudgetId(budgetId)
                    .setAmount(3.0)
                    .setComment("test_comments_1")
                    .setCreated(Instant.parse("2019-06-16T10:22:54.246625Z"));
            Expense secondExpense = new Expense()
                    .setId(UUID.randomUUID().toString())
                    .setBudgetId(budgetId)
                    .setAmount(4.0)
                    .setComment("test_comments_2")
                    .setCreated(Instant.parse("2019-06-16T10:28:23.053553Z"));
            ArrayList<Expense> expenseList = new ArrayList<>() {{
                add(firstExpense);
                add(secondExpense);
            }};
            Pageable pageable = PageRequest.of(0, 100);
            Page<Expense> page = new PageImpl<>(expenseList, pageable, 2);

            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            when(expenseRepository.findAllByBudgetId(eq(budgetId), any(Pageable.class)))
                    .thenReturn(page);
            // Then:
            MvcResult mvcResult = mockMvc.perform(get("/api/budget-mgt/v1/budgets/{budgetId}/expenses", budgetId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            String responseBodyAsString = mvcResult.getResponse().getContentAsString().trim();
            boolean responseBodyIsCorrect = Pattern.matches("\\{\"items\":\\[.*{1,}\\],\"_meta\":\\{\"elements\":2,\"pageSize\":100,\"page\":0,\"totalPages\":1,\"totalElements\":2\\}\\}", responseBodyAsString);
            assertThat(responseBodyIsCorrect).isTrue();
        }

        @Test
        void when_expenses_found_with_limit_1_then_1_expense_is_visible() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            Expense firstExpense = new Expense()
                    .setId(UUID.randomUUID().toString())
                    .setBudgetId(budgetId)
                    .setAmount(3.0)
                    .setComment("test_comments_1")
                    .setCreated(Instant.parse("2019-06-16T10:22:54.246625Z"));
            ArrayList<Expense> expenseList = new ArrayList<>();
            expenseList.add(firstExpense);
            Pageable pageable = PageRequest.of(0, 1);
            Page<Expense> page = new PageImpl<>(expenseList, pageable, 2);

            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            when(expenseRepository.findAllByBudgetId(eq(budgetId), any(Pageable.class)))
                    .thenReturn(page);
            // Then:
            MvcResult mvcResult = mockMvc.perform(get("/api/budget-mgt/v1/budgets/{budgetId}/expenses?limit=1", budgetId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            String responseBodyAsString = mvcResult.getResponse().getContentAsString().trim();
            boolean paginationInBodyIsCorrect = Pattern.matches(".*,\"_meta\":\\{\"elements\":1,\"pageSize\":1,\"page\":0,\"totalPages\":2,\"totalElements\":2\\}\\}", responseBodyAsString);
            boolean onlyOneExpenseIsVisible = Pattern.matches("\\{\"items\":\\[.*test_comments_1.*\\],.*", responseBodyAsString);
            org.junit.jupiter.api.Assertions.assertAll(
                    () -> assertThat(paginationInBodyIsCorrect).isTrue(),
                    () -> assertThat(onlyOneExpenseIsVisible).isTrue()
            );
        }

        @Test
        void when_expenses_found_with_page_1_then_pagination_metadata_is_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            Expense firstExpense = new Expense()
                    .setId(UUID.randomUUID().toString())
                    .setBudgetId(budgetId)
                    .setAmount(3.0)
                    .setComment("test_comments_1")
                    .setCreated(Instant.parse("2019-06-16T10:22:54.246625Z"));
            ArrayList<Expense> expenseList = new ArrayList<>();
            expenseList.add(firstExpense);
            Pageable pageable = PageRequest.of(1, 100);
            Page<Expense> page = new PageImpl<>(expenseList, pageable, 101);
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            when(expenseRepository.findAllByBudgetId(eq(budgetId), any(Pageable.class)))
                    .thenReturn(page);
            // Then:
            MvcResult mvcResult = mockMvc.perform(get("/api/budget-mgt/v1/budgets/{budgetId}/expenses?page=1", budgetId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            String responseBodyAsString = mvcResult.getResponse().getContentAsString().trim();
            boolean paginationInBodyIsCorrect = Pattern.matches(".*,\"_meta\":\\{\"elements\":1,\"pageSize\":100,\"page\":1,\"totalPages\":2,\"totalElements\":101\\}\\}", responseBodyAsString);
            boolean onlyOneExpenseIsVisible = Pattern.matches("\\{\"items\":\\[.*test_comments_1.*\\],.*", responseBodyAsString);
            org.junit.jupiter.api.Assertions.assertAll(
                    () -> assertThat(paginationInBodyIsCorrect).isTrue(),
                    () -> assertThat(onlyOneExpenseIsVisible).isTrue()
            );
        }

        @Test
        void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            String randomUUID = UUID.randomUUID().toString();
            mockMvc.perform(get("/api/budget-mgt/v1/budgets/{budgetId}/expenses", randomUUID))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class POST_Api_Budgets_Id_Expenses {
        // POST /api/budget-mgt/v1/budgets/{budgetId}/expenses
        @Test
        void when_expense_is_created_then_response_status_and_headers_are_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            OASExpenseCreation expenseCreation = new OASExpenseCreation()
                    .budgetId(budgetId)
                    .comment("comment")
                    .amount(1.0);
            Expense savedExpense = expenseMapper.toExpense(expenseCreation)
                    .setCreated(Instant.now())
                    .setId(UUID.randomUUID().toString());
            when(expenseRepository.save(any(Expense.class)))
                    .thenReturn(savedExpense);
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget()));
            // Then:
            mockMvc.perform(post("/api/budget-mgt/v1/budgets/{budgetId}/expenses", budgetId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", basicAuthHeaderValue)
                            .content(new ObjectMapper().writeValueAsString(expenseCreation)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void when_budgetId_does_not_match_then_response_status_and_headers_are_ok() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            Expense expenseInRequestBody = new Expense()
                    .setBudgetId(UUID.randomUUID().toString())
                    .setComment("comment")
                    .setAmount(1.0);

            // Then:
            mockMvc.perform(post("/api/budget-mgt/v1/budgets/{budgetId}/expenses", budgetId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", basicAuthHeaderValue)
                            .content(new ObjectMapper().writeValueAsString(expenseInRequestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(containsString("Budget id in body and path don't match.")));

        }

        @Test
        void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            String randomUUID = UUID.randomUUID().toString();
            mockMvc.perform(post("/api/budget-mgt/v1/budgets/{budgetId}/expenses", randomUUID, randomUUID))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class PUT_Api_Budgets_Id_Expenses_Id {
        // PUT /api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}
        @Test
        void when_expense_is_updated_then_response_is_ok() throws Exception {
            // Given:
            String expenseId = UUID.randomUUID().toString();
            String budgetId = UUID.randomUUID().toString();
            Expense originExpense = new Expense()
                    .setId(expenseId)
                    .setBudgetId(budgetId)
                    .setComment("comment")
                    .setAmount(1.0);
            Expense expenseInRequestBody = new Expense()
                    .setId(expenseId)
                    .setBudgetId(budgetId)
                    .setComment("edited_comment")
                    .setAmount(1.0);

            Expense mergedExpense = originExpense.mergeWith(expenseInRequestBody);
            // When:
            when(expenseRepository.findByIdAndBudgetId(expenseId, budgetId))
                    .thenReturn(Optional.of(originExpense));
            when(expenseRepository.save(any(Expense.class))).thenReturn(mergedExpense);
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));

            // Then:
            MvcResult mvcResult = mockMvc.perform(put("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", budgetId, expenseId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", basicAuthHeaderValue)
                            .content(new ObjectMapper().writeValueAsString(expenseInRequestBody)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            // And:
            String responseBody = mvcResult.getResponse().getContentAsString();
            Expense returnedExpense = new ObjectMapper().registerModule(new JavaTimeModule())
                    .readValue(responseBody, Expense.class);
            assertThat(returnedExpense).isNotNull()
                    .isEqualTo(mergedExpense);
        }

        @Test
        void when_expense_is_not_found_during_update_then_response_is_ok() throws Exception {
            // Given:
            String expenseId = UUID.randomUUID().toString();
            String budgetId = UUID.randomUUID().toString();
            Expense expenseInRequestBody = new Expense()
                    .setId(expenseId)
                    .setBudgetId(budgetId)
                    .setComment("edited_comment")
                    .setAmount(1.0);
            // When:
            when(expenseRepository.findByIdAndBudgetId(expenseId, budgetId)).thenReturn(Optional.empty());
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            // Then:
            MvcResult mvcResult = mockMvc.perform(put("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", budgetId, expenseId)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", basicAuthHeaderValue)
                            .content(new ObjectMapper().writeValueAsString(expenseInRequestBody)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            // And:
            String responseBody = mvcResult.getResponse().getContentAsString();
            ErrorDescription errorDescription = new ObjectMapper().registerModule(new JavaTimeModule())
                    .readValue(responseBody, ErrorDescription.class);
            assertThat(errorDescription).isNotNull();
            org.junit.jupiter.api.Assertions.assertAll(
                    () -> assertThat(errorDescription.getStatus()).isEqualTo(404),
                    () -> assertThat(errorDescription.getMessage()).isEqualTo("Expense with id: " + expenseId + " not found in budget with id: " + budgetId + "."),
                    () -> assertThat(errorDescription.getStatusName()).isEqualTo("NOT_FOUND"),
                    () -> assertThat(errorDescription.getTimestamp()).isInstanceOf(Instant.class)
            );
        }

        @Test
        void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            String randomUUID = UUID.randomUUID().toString();
            mockMvc.perform(put("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", randomUUID, randomUUID))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DELETE_Api_Budgets_Id_Expenses_Id {
        // DELETE /api/budget-mgt/v1/budgets/{budgetId}/expenses/{id}
        @Test
        void when_expense_found_during_deletion_then_response_is_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String expenseId = UUID.randomUUID().toString();
            doNothing().when(expenseRepository).deleteByIdAndBudgetId(expenseId, budgetId);
            when(expenseRepository.findByIdAndBudgetId(expenseId, budgetId)).thenReturn(Optional.of(new Expense()));
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));

            // Then:
            mockMvc.perform(delete("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", budgetId, expenseId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andExpect(status().isNoContent());
        }

        @Test
        void when_expense_not_found_during_deletion_then_response_is_correct() throws Exception {
            // Given:
            String budgetId = UUID.randomUUID().toString();
            String expenseId = UUID.randomUUID().toString();
            when(budgetRepository.findById(budgetId))
                    .thenReturn(Optional.of(new Budget().setId(budgetId)));
            doThrow(EmptyResultDataAccessException.class)
                    .when(expenseRepository).deleteByIdAndBudgetId(expenseId, budgetId);

            // Then:
            mockMvc.perform(delete("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", budgetId, expenseId)
                            .header("Authorization", basicAuthHeaderValue))
                    .andExpect(status().isNotFound())
                    .andDo(print())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(containsString("Expense with id: " + expenseId + " not found in budget with id: " + budgetId + ".")));
        }

        @Test
        void whenBAHeaderIsMissingThenUnauthorized() throws Exception {
            String randomUUID = UUID.randomUUID().toString();
            mockMvc.perform(delete("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", randomUUID, randomUUID))
                    .andExpect(status().isUnauthorized());
        }
    }
}
