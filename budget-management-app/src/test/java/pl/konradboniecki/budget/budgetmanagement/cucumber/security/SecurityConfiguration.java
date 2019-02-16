package pl.konradboniecki.budget.budgetmanagement.cucumber.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class SecurityConfiguration {

    @Bean
    @Profile("test")
    public SecurityOnMavenBuild securityOnMavenBuild() {
        log.info("SETUP -> Initializing SecurityOnMavenBuild");
        return new SecurityOnMavenBuild();
    }

    @Bean
    @Profile("acceptance-tests")
    public SecurityOnDeployment securityOnDeployment() {
        log.info("SETUP -> Initializing SecurityOnDeployment");
        return new SecurityOnDeployment();
    }
}
