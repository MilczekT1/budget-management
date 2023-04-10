package pl.konradboniecki.budget.budgetmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import pl.konradboniecki.chassis.ChassisApplication;

@EnableMongoRepositories
@ChassisApplication
public class BudgetManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetManagementApplication.class, args);
    }
}
