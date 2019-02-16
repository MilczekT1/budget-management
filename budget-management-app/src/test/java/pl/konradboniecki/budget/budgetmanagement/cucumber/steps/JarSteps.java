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
import pl.konradboniecki.budget.budgetmanagement.controller.JarController;
import pl.konradboniecki.budget.budgetmanagement.cucumber.commons.SharedData;
import pl.konradboniecki.budget.budgetmanagement.cucumber.security.Security;
import pl.konradboniecki.budget.budgetmanagement.feature.jar.JarMapper;
import pl.konradboniecki.budget.openapi.dto.model.OASJar;
import pl.konradboniecki.budget.openapi.dto.model.OASJarCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASJarModification;
import pl.konradboniecki.budget.openapi.dto.model.OASJarPage;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class JarSteps {

    private final Security security;
    private final TestRestTemplate testRestTemplate;
    private final SharedData sharedData;
    private final JarMapper jarMapper;

    @When("I create jar with properties:")
    public void iCreateJarWithProperties(DataTable dataTable) {
        List<OASJar> jars = dataTable.asList(OASJar.class);
        jars.forEach((jar) -> {
            OASJarCreation jarCreation = new OASJarCreation()
                    .budgetId(jar.getBudgetId())
                    .capacity(jar.getCapacity())
                    .jarName(jar.getJarName())
                    .currentAmount(jar.getCurrentAmount());
            HttpEntity<OASJarCreation> entity = new HttpEntity<>(jarCreation, security.getSecurityHeaders());
            ResponseEntity<OASJar> responseEntity = testRestTemplate
                    .exchange(JarController.BASE_PATH + "/budgets/{budgetId}/jars", HttpMethod.POST, entity, OASJar.class, jar.getBudgetId());
            assertThat(responseEntity.getBody()).isNotNull();
            OASJar savedJar = responseEntity.getBody();
            sharedData.setLastResponseEntity(responseEntity);
            sharedData.addJarNameToJarEntry(savedJar.getJarName(), savedJar);
            sharedData.addJarIdToBudgetIdEntry(savedJar.getId(), jar.getBudgetId());
        });
    }

    @Then("jar is created")
    public void jarIsCreated() {
        responseStatusCodeEquals(HttpStatus.CREATED);
    }

    private void responseStatusCodeEquals(HttpStatus httpStatus) {
        HttpStatus lastResponseHttpStatus = sharedData.getLastResponseEntity().getStatusCode();
        assertThat(lastResponseHttpStatus).isEqualTo(httpStatus);
    }

    @When("I delete jar with name (.+) by id from (my|random) budget$")
    public void iDeleteJarWithNameNot_existing_jarByIdFromMyBudget(String jarName, String whichBudget) {
        OASJar jar = sharedData.getJarNameToJarMap().getOrDefault(jarName, generateJar(whichBudget));
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<OASJar> responseEntity = testRestTemplate
                .exchange(JarController.BASE_PATH + "/budgets/{budgetId}/jars/{jarId}", HttpMethod.DELETE, entity, OASJar.class, jar.getBudgetId(), jar.getId());
        sharedData.setLastResponseEntity(responseEntity);
    }

    private OASJar generateJar(String whichBudget) {
        String budgetId = whichBudget.equals("my")
                ? SharedData.myExistingBudget.getId() : UUID.randomUUID().toString();
        return new OASJar()
                .budgetId(budgetId)
                .id(SharedData.getLastCommonRandomIdOrGenerateNewIfEmpty());
    }

    @Then("jar is deleted")
    public void jarIsDeleted() {
        responseStatusCodeEquals(HttpStatus.NO_CONTENT);
    }

    @Then("jar is not found")
    public void jarIsNotFound() {
        responseStatusCodeEquals(HttpStatus.NOT_FOUND);
    }

    @Then("jar is found")
    public void jarIsFound() {
        responseStatusCodeEquals(HttpStatus.OK);
    }

    @When("I search jar with name (.+) by id from (my|random) budget$")
    public void iSearchJarWithNameHolidaysByIdFromMyBudget(String jarName, String whichBudget) {
        OASJar jar = sharedData.getJarNameToJarMap().getOrDefault(jarName, generateJar(whichBudget));
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<OASJar> responseEntity = testRestTemplate
                .exchange(JarController.BASE_PATH + "/budgets/{budgetId}/jars/{jarId}", HttpMethod.GET, entity, OASJar.class, jar.getBudgetId(), jar.getId());
        sharedData.setLastResponseEntity(responseEntity);
    }

    @And("response contains jar with following properties")
    public void responseContainsJarWithFollowingProperties(DataTable dataTable) {
        OASJar expectedJar = (OASJar) dataTable.asList(OASJar.class).get(0);
        OASJar foundJar = (OASJar) sharedData.getLastResponseEntity().getBody();
        Assertions.assertAll(
                () -> assertThat(foundJar).isNotNull(),
                () -> assertThat(expectedJar).isNotNull(),
                () -> assertThat(foundJar.getBudgetId())
                        .isEqualTo(expectedJar.getBudgetId()),
                () -> assertThat(foundJar.getJarName())
                        .isEqualTo(expectedJar.getJarName()),
                () -> assertThat(foundJar.getCurrentAmount())
                        .isEqualTo(expectedJar.getCurrentAmount()),
                () -> assertThat(foundJar.getCapacity())
                        .isEqualTo(expectedJar.getCapacity()));
    }

    @When("I search jars from (my|random) budget$")
    public void iSearchJarsFromMyBudget(String whichBudget) {
        String budgetId;
        if ("my".equals(whichBudget)) {
            budgetId = SharedData.myExistingBudget.getId();
        } else {
            budgetId = UUID.randomUUID().toString();
        }
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<OASJarPage> responseEntity = testRestTemplate
                .exchange(JarController.BASE_PATH + "/budgets/{budgetId}/jars", HttpMethod.GET, entity, OASJarPage.class, budgetId);
        sharedData.setLastResponseEntity(responseEntity);
    }

    @Then("jars no longer exist")
    public void jarsNoLongerExist() {
        responseStatusCodeEquals(HttpStatus.OK);
        OASJarPage jarList = (OASJarPage) sharedData.getLastResponseEntity().getBody();
        assertThat(jarList).isNotNull();
        assertThat(jarList.getItems().size()).isEqualTo(0);
    }

    @Then("jars are found")
    public void jarsAreFound() {
        responseStatusCodeEquals(HttpStatus.OK);
        OASJarPage jarList = (OASJarPage) sharedData.getLastResponseEntity().getBody();
        assertThat(jarList).isNotNull();
        assertThat(jarList.getItems().size()).isGreaterThan(0);
    }

    @When("I update jar with name (.+) by id from (my|random) budget with properties:$")
    public void iUpdateJarWithNameNot_existing_jarByIdFromRandomBudgetWithProperties(String jarName, String whichBudget, DataTable dataTable) {
        OASJar previousJar = sharedData.getJarNameToJarMap().getOrDefault(jarName, generateJar(whichBudget));
        OASJar newJar = (OASJar) dataTable.asList(OASJar.class).get(0);
        newJar.setId(previousJar.getId());
        String budgetId = myOrRandomBudgetId(whichBudget);
        OASJarModification jarModification = new OASJarModification()
                .id(newJar.getId())
                .budgetId(newJar.getBudgetId())
                .jarName(newJar.getJarName())
                .capacity(newJar.getCapacity())
                .currentAmount(newJar.getCurrentAmount())
                .status(newJar.getStatus());
        HttpEntity<?> entity = new HttpEntity<>(jarModification, security.getSecurityHeaders());
        ResponseEntity<OASJar> responseEntity = testRestTemplate
                .exchange(JarController.BASE_PATH + "/budgets/{budgetId}/jars/{jarId}", HttpMethod.PUT, entity, OASJar.class, budgetId, newJar.getId());
        sharedData.setLastResponseEntity(responseEntity);
    }

    @Then("response contains {int} jars")
    public void responseContainsJars(Integer amountOfJars) {
        OASJarPage jarList = (OASJarPage) sharedData.getLastResponseEntity().getBody();
        assertThat(jarList.getItems().size()).isEqualTo(amountOfJars);
    }

    private String myOrRandomBudgetId(String whichBudget) {
        if (whichBudget.equals("my")) {
            return SharedData.myExistingBudget.getId();
        } else {
            return SharedData.getLastCommonRandomIdOrGenerateNewIfEmpty();
        }
    }
}
