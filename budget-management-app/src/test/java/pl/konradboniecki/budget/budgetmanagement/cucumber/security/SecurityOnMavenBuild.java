package pl.konradboniecki.budget.budgetmanagement.cucumber.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

@Slf4j
public class SecurityOnMavenBuild implements Security {

    private HttpHeaders securityHeaders = new HttpHeaders();
    @Autowired
    private ChassisSecurityBasicAuthHelper chassisSecurityBasicAuthHelper;

    @Override
    public HttpHeaders getSecurityHeaders() {
        return securityHeaders;
    }

    @Override
    public void basicAuthentication() {
        String baToken = chassisSecurityBasicAuthHelper.getBasicAuthHeaderValue();
        securityHeaders.set(HttpHeaders.AUTHORIZATION, baToken);
    }

    @Override
    public void unathorize() {
        securityHeaders = new HttpHeaders();
    }
}
