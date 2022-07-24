package pl.konradboniecki.budget.budgetmanagement.cucumber.commons;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Configuration
@EnableConfigurationProperties(AcceptanceTestsProperties.class)
public class Config {

    @Value("${local.server.port:0}")
    int localServerPort;
    @Autowired
    private AcceptanceTestsProperties acceptanceTestsProperties;

    @Bean
    @Profile("test")
    public TestRestTemplate testRestTemplateOnMavenBuild() {
        log.info("SETUP -> Initializing TestRestTemplate on maven build");
        String rootUri = "http://localhost:" + localServerPort;
        TestRestTemplate testRestTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri(rootUri));
        testRestTemplate.getRestTemplate().getInterceptors().add(new CucumberInterceptor());
        return testRestTemplate;
    }

    @Bean
    @Profile("acceptance-tests")
    public TestRestTemplate testRestTemplateOnDeployment(final ClientHttpRequestFactory clientHttpRequestFactory) {
        log.info("SETUP -> Initializing TestRestTemplate on deployment");
        String rootUri = acceptanceTestsProperties.getBaseUrl();
        assertThat(rootUri).isNotNull();

        final var testRestTemplateBuilder = new RestTemplateBuilder()
                .requestFactory(() -> clientHttpRequestFactory)
                .rootUri(rootUri).additionalInterceptors();

        TestRestTemplate testRestTemplate = new TestRestTemplate(testRestTemplateBuilder);
        testRestTemplate.getRestTemplate().getInterceptors().add(new CucumberInterceptor());
        return testRestTemplate;
    }

    @Bean
    @Profile("acceptance-tests")
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        final var httpClient = HttpClients.custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        final var requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }
}
