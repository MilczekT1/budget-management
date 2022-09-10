package pl.konradboniecki.budget.budgetmanagement.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.konradboniecki.budget.budgetmanagement.cucumber.commons.SharedData;
import pl.konradboniecki.budget.budgetmanagement.cucumber.security.Security;
import pl.konradboniecki.budget.budgetmanagement.feature.expense.ExpenseMapper;
import pl.konradboniecki.budget.openapi.dto.model.OASExpense;
import pl.konradboniecki.budget.openapi.dto.model.OASExpenseCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASExpenseModification;
import pl.konradboniecki.budget.openapi.dto.model.OASExpensePage;

import java.util.List;
import java.util.UUID;

import static io.cucumber.java.en.Then.Thens;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class ExpenseSteps {

    private final Security security;
    private final TestRestTemplate testRestTemplate;
    private final SharedData sharedData;
    private final ExpenseMapper expenseMapper;

    private void responseStatusCodeEquals(HttpStatus httpStatus) {
        HttpStatus lastResponseHttpStatus = sharedData.getLastResponseEntity().getStatusCode();
        assertThat(lastResponseHttpStatus).isEqualTo(httpStatus);
    }

    @When("I create expense with properties:")
    public void iCreateExpenseWithProperties(DataTable dataTable) {
        List<OASExpense> expenses = dataTable.asList(OASExpense.class);
        expenses.forEach((expense) -> {
            OASExpenseCreation expenseCreation = new OASExpenseCreation()
                    .budgetId(expense.getBudgetId())
                    .amount(expense.getAmount())
                    .comment(expense.getComment());
            HttpEntity<OASExpenseCreation> entity = new HttpEntity<>(expenseCreation, security.getSecurityHeaders());
            ResponseEntity<OASExpense> responseEntity = testRestTemplate
                    .exchange("/api/budget-mgt/v1/budgets/{budgetId}/expenses", HttpMethod.POST, entity, OASExpense.class, expense.getBudgetId());
            if (!responseEntity.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                assertThat(responseEntity.getBody()).isNotNull();
                OASExpense exp = responseEntity.getBody();
                sharedData.addExpenseIdToBudgetIdEntry(exp.getId(), expense.getBudgetId());
                sharedData.addCommentToExpenseEntry(exp.getComment(), exp);
            }
            sharedData.setLastResponseEntity(responseEntity);
        });
    }

    @When("I delete expense with comment (.+) by id from (my|random) budget$")
    public void iDeleteExpenseWithCommentById(String comment, String whichBudget) {
        OASExpense expense = sharedData.getCommentToExpenseMap().getOrDefault(comment, generateExpense(whichBudget));
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<OASExpense> responseEntity = testRestTemplate
                .exchange("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", HttpMethod.DELETE, entity, OASExpense.class, expense.getBudgetId(), expense.getId());
        sharedData.setLastResponseEntity(responseEntity);
    }

    @Then("expense is deleted")
    public void expenseIsDeleted() {
        responseStatusCodeEquals(HttpStatus.NO_CONTENT);
    }

    private OASExpense generateExpense(String whichBudget) {
        String budgetId = whichBudget.equals("my")
                ? SharedData.myExistingBudget.getId() : UUID.randomUUID().toString();
        return new OASExpense()
                .budgetId(budgetId)
                .id(UUID.randomUUID().toString());
    }

    @When("I search expense with comment (.+) by id from (my|random) budget$")
    public void iSearchExpenseWithCommentByIdFromMyBudget(String comment, String whichBudget) {
        OASExpense expense = sharedData.getCommentToExpenseMap().getOrDefault(comment, generateExpense(whichBudget));
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<OASExpense> responseEntity = testRestTemplate
                .exchange("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", HttpMethod.GET, entity, OASExpense.class, expense.getBudgetId(), expense.getId());
        sharedData.setLastResponseEntity(responseEntity);
    }

    @When("I search expenses from (my|random) budget$")
    public void iSearchExpensesFromMyBudget(String whichBudget) {
        String budgetId;
        if ("my".equals(whichBudget)) {
            budgetId = SharedData.myExistingBudget.getId();
        } else {
            budgetId = UUID.randomUUID().toString();
        }
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());

        ResponseEntity<OASExpensePage> responseEntity = testRestTemplate
                .exchange("/api/budget-mgt/v1/budgets/{budgetId}/expenses", HttpMethod.GET, entity,
                        OASExpensePage.class, budgetId);
        sharedData.setLastResponseEntity(responseEntity);
    }

    @Thens({
            @Then("expense is found"),
            @Then("expenses are found")
    })
    public void expensesAreFound() {
        responseStatusCodeEquals(HttpStatus.OK);
    }

    @Thens({
            @Then("expenses are not found"),
            @Then("expense is not found")
    })
    public void expensesAreNotFound() {
        responseStatusCodeEquals(HttpStatus.NOT_FOUND);
    }

    @Then("expense is created")
    public void expenseIsCreated() {
        responseStatusCodeEquals(HttpStatus.CREATED);
    }

    @Then("expenses no longer exist")
    public void expensesNoLongerExist() {
        responseStatusCodeEquals(HttpStatus.OK);
        OASExpensePage expenseList = (OASExpensePage) sharedData.getLastResponseEntity().getBody();
        assertThat(expenseList).isNotNull();
        assertThat(expenseList.getItems()).isEmpty();
    }

    @When("I update expense with comment (.+) by id from (my|random) budget with properties:$")
    public void iUpdateExpenseWithCommentCommentByIdFromMyBudgetWithProperties(String comment, String whichBudget, DataTable dataTable) {
        OASExpense previousExpense = sharedData.getCommentToExpenseMap().getOrDefault(comment, generateExpense(whichBudget));
        OASExpense newExpense = (OASExpense) dataTable.asList(OASExpense.class).get(0);
        newExpense.setId(previousExpense.getId());
        OASExpenseModification expenseModification = new OASExpenseModification()
                .id(newExpense.getId())
                .budgetId(newExpense.getBudgetId())
                .amount(newExpense.getAmount())
                .comment(newExpense.getComment())
                .created(newExpense.getCreated());
        String budgetId = myOrRandomBudgetId(whichBudget);
        HttpEntity<OASExpenseModification> entity = new HttpEntity<>(expenseModification, security.getSecurityHeaders());
        ResponseEntity<OASExpense> responseEntity = testRestTemplate
                .exchange("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}", HttpMethod.PUT, entity, OASExpense.class, budgetId, newExpense.getId());
        sharedData.setLastResponseEntity(responseEntity);
    }

    @Then("the operation is a success")
    public void theOperationIsASuccess() {
        HttpStatus lastResponseHttpStatus = sharedData.getLastResponseEntity().getStatusCode();
        assertThat(lastResponseHttpStatus.is2xxSuccessful()).isTrue();
    }

    @Then("operation is a failure")
    public void operationIsAFailure() {
        HttpStatus lastResponseHttpStatus = sharedData.getLastResponseEntity().getStatusCode();
        assertThat(lastResponseHttpStatus.is4xxClientError()).isTrue();
    }

    private String myOrRandomBudgetId(String whichBudget) {
        if (whichBudget.equals("my")) {
            return SharedData.myExistingBudget.getId();
        } else {
            return UUID.randomUUID().toString();
        }
    }

    @And("response contains expense with following properties")
    public void responseContainsExpenseWithFollowingProperties(DataTable dataTable) {
        OASExpense expectedExpense = (OASExpense) dataTable.asList(OASExpense.class).get(0);
        OASExpense foundExpense = (OASExpense) sharedData.getLastResponseEntity().getBody();
        Assertions.assertAll(
                () -> assertThat(foundExpense).isNotNull(),
                () -> assertThat(expectedExpense).isNotNull(),
                () -> assertThat(foundExpense.getBudgetId())
                        .isEqualTo(expectedExpense.getBudgetId()),
                () -> assertThat(foundExpense.getComment())
                        .isEqualTo(expectedExpense.getComment()),
                () -> assertThat(foundExpense.getAmount())
                        .isEqualTo(expectedExpense.getAmount()));
    }

    @And("response contains {int} expenses")
    public void responseContainsExpenses(int amountOfExpenses) {
        OASExpensePage expenseList = (OASExpensePage) sharedData.getLastResponseEntity().getBody();
        assertThat(expenseList.getItems()).hasSize(amountOfExpenses);
    }
}
