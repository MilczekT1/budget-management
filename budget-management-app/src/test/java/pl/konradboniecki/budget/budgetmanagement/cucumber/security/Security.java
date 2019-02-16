package pl.konradboniecki.budget.budgetmanagement.cucumber.security;

import org.springframework.http.HttpHeaders;

public interface Security {

    HttpHeaders getSecurityHeaders();

    void basicAuthentication();

    void unathorize();

}
