package pl.konradboniecki.budget.budgetmanagement.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.konradboniecki.budget.budgetmanagement.cucumber.commons.SharedData;
import pl.konradboniecki.budget.budgetmanagement.cucumber.security.Security;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.Budget;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.BudgetMapper;
import pl.konradboniecki.budget.openapi.dto.model.OASBudget;
import pl.konradboniecki.budget.openapi.dto.model.OASBudgetCreation;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class BudgetSteps {

    private final Security security;
    private final TestRestTemplate testRestTemplate;
    private final SharedData sharedData;
    private final BudgetMapper budgetMapper;

    @After
    public void scenarioCleanup() {
        security.basicAuthentication();

        sharedData.getExpenseIdToBudgetIdMapToDelete().keySet().stream()
                .filter(Objects::nonNull)
                .forEach((expenseId) -> {
                    String budgetId = sharedData.getExpenseIdToBudgetIdMapToDelete().get(expenseId);
                    cleanExpense(budgetId, expenseId);
                });
        sharedData.clearExpenseIdToBudgetIdMapToDelete();
        sharedData.clearCommentToExpenseId();

        sharedData.getJarIdToBudgetIdMapToDelete().keySet().stream()
                .filter(Objects::nonNull)
                .forEach((jarId) -> {
                    String budgetId = sharedData.getJarIdToBudgetIdMapToDelete().get(jarId);
                    cleanJar(budgetId, jarId);
                });
        sharedData.clearJarNameToJarMap();
        sharedData.clearJarIdToBudgetIdMapToDelete();

        sharedData.getBudgetIdsToDelete().stream()
                .filter(Objects::nonNull)
                .forEach(this::cleanBudget);
        sharedData.clearBudgetIdsToDelete();
    }

    private void cleanExpense(String budgetId, String expenseId) {
        log.info("SCENARIO CLEANUP: Deleting expense from budget with ids: {}/{}", expenseId, budgetId);
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange("/api/budget-mgt/v1/budgets/{budgetId}/expenses/{expenseId}",
                HttpMethod.DELETE, entity, Void.class, budgetId, expenseId);
        log.info("SCENARIO CLEANUP: result {}", responseEntity.getStatusCodeValue());
        assertThat(responseEntity.getStatusCode())
                .isIn(HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND);
    }

    private void cleanBudget(String budgetId) {
        log.info("SCENARIO CLEANUP: Deleting budget with id: {}", budgetId);
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange("/api/budget-mgt/v1/budgets/{budgetId}", HttpMethod.DELETE, entity, Void.class, budgetId);
        log.info("SCENARIO CLEANUP: result {}", responseEntity.getStatusCodeValue());
        assertThat(responseEntity.getStatusCode())
                .isIn(HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND);
    }

    private void cleanJar(String budgetId, String jarId) {
        log.info("SCENARIO CLEANUP: Deleting jar from budget with ids: {}/{}", jarId, budgetId);
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange("/api/budget-mgt/v1/budgets/{budgetId}/jars/{jarId}",
                HttpMethod.DELETE, entity, Void.class, budgetId, jarId);
        log.info("SCENARIO CLEANUP: result {}", responseEntity.getStatusCodeValue());
        assertThat(responseEntity.getStatusCode())
                .isIn(HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND);
    }

    @When("I create a budget for family (.+) with properties:$")
    public void iCreateABudgetForFamilyWithProperties(String familyName, DataTable dataTable) {
        String familyId = sharedData.getFamilyIdForName(familyName);
        List<OASBudget> budgets = dataTable.asList(OASBudget.class);
        assertThat(budgets.get(0).getFamilyId()).isEqualTo(familyId);
        OASBudgetCreation budgetToSave = new OASBudgetCreation()
                .familyId(budgets.get(0).getFamilyId())
                .maxJars(budgets.get(0).getMaxJars());

        HttpEntity<?> entity = new HttpEntity<>(budgetToSave, security.getSecurityHeaders());
        ResponseEntity<OASBudget> responseEntity = testRestTemplate
                .exchange("/api/budget-mgt/v1/budgets", HttpMethod.POST, entity, OASBudget.class);
        if (!responseEntity.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            assertThat(responseEntity.getBody()).isNotNull();
            sharedData.addBudgetIdToDelete(responseEntity.getBody().getId());
        }
        sharedData.setLastResponseEntity(responseEntity);
    }

    @Then("budget is created")
    public void theBudgetIsCreated() {
        responseStatusCodeEquals(HttpStatus.CREATED);
    }

    @Then("budget is not created")
    public void budgetIsNotCreated() {
        HttpStatus lastResponseHttpStatus = sharedData.getLastResponseEntity().getStatusCode();
        assertThat(lastResponseHttpStatus.is4xxClientError()).isTrue();
    }

    @And("family (.+) already have a budget$")
    public void familyAlreadyHaveABudget(String familyName) {
        String familyId = sharedData.getFamilyIdForName(familyName);
        OASBudgetCreation budgetToSave = new OASBudgetCreation()
                .familyId(familyId)
                .maxJars(6L);
        HttpEntity<?> entity = new HttpEntity<>(budgetToSave, security.getSecurityHeaders());
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange("/api/budget-mgt/v1/budgets", HttpMethod.POST, entity, OASBudget.class);
        sharedData.setLastResponseEntity(responseEntity);
        responseStatusCodeEquals(HttpStatus.CREATED);

        OASBudget bgt = (OASBudget) responseEntity.getBody();
        assertThat(bgt).isNotNull();
        assertThat(bgt.getId()).isNotNull();
        sharedData.addBudgetIdToDelete(bgt.getId());
    }

    @When("I delete a budget for family (.+)$")
    public void iDeleteABudgetFor_family(String familyName) {
        String familyId = sharedData.getFamilyIdForName(familyName);
        OASBudget bgt = findBudgetByFamilyId(familyId);
        if (bgt != null && bgt.getId() != null) {
            HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
            ResponseEntity<?> responseEntity = testRestTemplate
                    .exchange("/api/budget-mgt/v1/budgets/{budgetId}", HttpMethod.DELETE, entity, OASBudget.class, bgt.getId());
            sharedData.setLastResponseEntity(responseEntity);
        }
    }

    @Then("budget is deleted")
    public void budgetIsDeleted() {
        responseStatusCodeEquals(HttpStatus.NO_CONTENT);
    }

    @And("family (.+) doesn't have a budget$")
    public void familyDoesnTHaveABudget(String familyName) {
        String familyId = sharedData.getFamilyIdForName(familyName);
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange("/api/budget-mgt/v1/budgets/" + familyId + "?idType=family", HttpMethod.GET, entity, OASBudget.class);
        sharedData.setLastResponseEntity(responseEntity);
        responseStatusCodeEquals(HttpStatus.NOT_FOUND);
    }

    @Then("budget is not found")
    public void budgetIsNotFound() {
        responseStatusCodeEquals(HttpStatus.NOT_FOUND);
    }

    @Then("budget is found")
    public void budgetIsFound() {
        responseStatusCodeEquals(HttpStatus.OK);
    }

    private OASBudget findBudgetByFamilyId(String id) {
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange("/api/budget-mgt/v1/budgets/" + id + "?idType=family", HttpMethod.GET, entity, OASBudget.class);
        sharedData.setLastResponseEntity(responseEntity);
        return (OASBudget) responseEntity.getBody();
    }

    @When("I get a budget for family (.+)$")
    public void iGetABudgetForFamilyMy_family(String familyName) {
        String familyId = sharedData.getFamilyIdForName(familyName);
        findBudgetByFamilyId(familyId);
    }

    private OASBudget findBudgetById(String id) {
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange("/api/budget-mgt/v1/budgets/{budgetId}", HttpMethod.GET, entity, OASBudget.class, id);
        sharedData.setLastResponseEntity(responseEntity);
        return (OASBudget) responseEntity.getBody();
    }

    private void responseStatusCodeEquals(HttpStatus httpStatus) {
        HttpStatus lastResponseHttpStatus = sharedData.getLastResponseEntity().getStatusCode();
        if (!lastResponseHttpStatus.equals(httpStatus)) {
            if (sharedData.getLastResponseEntity().getBody() != null) {
                log.error("last failed body: {}", sharedData.getLastResponseEntity().getBody().toString());
            }
            log.error("last headers: {}", sharedData.getLastResponseEntity().getHeaders());
        }
        assertThat(lastResponseHttpStatus).isEqualTo(httpStatus);
    }

    @And("I have already created a budget")
    public void iHaveAlreadyCreatedABudget() {
        Budget budgetToSave = new Budget()
                .setFamilyId(UUID.randomUUID().toString())
                .setMaxJars(6L);

        HttpEntity<?> entity = new HttpEntity<>(budgetToSave, security.getSecurityHeaders());
        ResponseEntity<OASBudget> responseEntity = testRestTemplate
                .exchange("/api/budget-mgt/v1/budgets", HttpMethod.POST, entity, OASBudget.class);
        sharedData.setLastResponseEntity(responseEntity);
        assertThat(responseEntity.getBody()).isNotNull();
        sharedData.addBudgetIdToDelete(responseEntity.getBody().getId());
        SharedData.setMyExistingBudget(responseEntity.getBody());
    }
}
