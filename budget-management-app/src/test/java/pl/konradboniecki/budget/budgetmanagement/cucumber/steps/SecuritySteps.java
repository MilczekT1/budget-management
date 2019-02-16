package pl.konradboniecki.budget.budgetmanagement.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.konradboniecki.budget.budgetmanagement.cucumber.commons.SharedData;
import pl.konradboniecki.budget.budgetmanagement.cucumber.security.Security;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class SecuritySteps {

    private final Security security;
    private final SharedData sharedData;

    @Given("I'm not authenticated")
    public void iMNotAuthenticated() {
        security.unathorize();
    }

    @Then("the operation is unauthorized")
    public void theOperationIsUnauthorized() {
        int lastResponseCodeValue = sharedData.getLastResponseEntity().getStatusCodeValue();
        assertThat(lastResponseCodeValue).isEqualTo(401);
    }

    @Given("I'm authenticated with Basic Auth")
    public void iMAuthenticatedWithBasicAuth() {
        security.basicAuthentication();
    }
}
