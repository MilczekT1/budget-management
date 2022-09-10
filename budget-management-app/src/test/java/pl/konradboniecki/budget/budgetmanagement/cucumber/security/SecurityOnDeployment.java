package pl.konradboniecki.budget.budgetmanagement.cucumber.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Objects;

@Slf4j
public class SecurityOnDeployment implements Security {

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
        Objects.requireNonNull(baToken);
        securityHeaders.set(HttpHeaders.AUTHORIZATION, baToken);
    }

    @Override
    public void unathorize() {
        securityHeaders = new HttpHeaders();
    }
}
