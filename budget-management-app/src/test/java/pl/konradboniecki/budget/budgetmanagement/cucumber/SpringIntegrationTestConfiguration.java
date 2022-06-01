package pl.konradboniecki.budget.budgetmanagement.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@Slf4j
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.lazy-initialization=true"
        })
@CucumberContextConfiguration
public class SpringIntegrationTestConfiguration {

    @LocalServerPort
    int localServerPort;

}
