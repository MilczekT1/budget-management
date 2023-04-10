package pl.konradboniecki.budget.budgetmanagement.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

import java.util.Arrays;

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

    @Rule
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry, Environment environment) {
        if (Arrays.asList(environment.getProfiles()).contains("acceptance-tests")) {
            return;
        } else {
            mongoDBContainer.start();
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        }
    }

}
