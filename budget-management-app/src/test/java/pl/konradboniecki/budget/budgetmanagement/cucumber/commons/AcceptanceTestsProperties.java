package pl.konradboniecki.budget.budgetmanagement.cucumber.commons;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tests.acceptance")
public class AcceptanceTestsProperties {

    private String baseUrl;
}
