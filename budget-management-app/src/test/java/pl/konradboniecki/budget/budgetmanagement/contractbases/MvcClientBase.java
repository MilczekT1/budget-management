package pl.konradboniecki.budget.budgetmanagement.contractbases;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.budgetmanagement.BudgetManagementApplication;
import pl.konradboniecki.budget.budgetmanagement.exception.FamilyConflictException;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.Budget;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.BudgetRepository;
import pl.konradboniecki.budget.budgetmanagement.feature.expense.Expense;
import pl.konradboniecki.budget.budgetmanagement.feature.expense.ExpenseRepository;
import pl.konradboniecki.budget.budgetmanagement.feature.jar.Jar;
import pl.konradboniecki.budget.budgetmanagement.feature.jar.JarRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.RestAssuredConfig.config;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = BudgetManagementApplication.class,
        webEnvironment = WebEnvironment.RANDOM_PORT
)
public abstract class MvcClientBase {
    @LocalServerPort
    int port;

    @MockBean
    private JarRepository jarRepository;
    @MockBean
    private BudgetRepository budgetRepository;
    @MockBean
    private ExpenseRepository expenseRepository;

    @BeforeEach
    public void setUpMocks() {
        RestAssured.baseURI = "http://localhost:" + this.port;
        RestAssured.config = config().redirect(redirectConfig().followRedirects(false));
        mock_jar_delete();
        mock_jar_save();
        mock_jar_update();
        mock_jar_find();
        mock_jar_find_all();

        mock_budget_find();
        mock_budget_save();

        mock_expense_delete();
        mock_expense_save();
        mock_expense_find_all();
    }

    private void mock_jar_delete() {
        String budgetIdWithJar = "38410e86-5782-4390-b026-184558177c5f";
        String budgetIdWithoutJars = "38410e86-5782-4390-b026-184558177c5f";
        String deletedJarId = "666b975b-da45-4552-975b-26c559eb6b28";
        String absentJarId = "70fc6180-201e-4c7a-918e-095f8e9bfada";
        when(budgetRepository.findById(budgetIdWithJar))
                .thenReturn(Optional.of(new Budget().setId(budgetIdWithJar)));

        when(jarRepository.deleteJarByIdAndBudgetId(deletedJarId, budgetIdWithJar))
                .thenReturn(1L);

        when(jarRepository.deleteJarByIdAndBudgetId(absentJarId, budgetIdWithoutJars))
                .thenReturn(0L);
    }
    private void mock_jar_save() {
        String budgetId = "39612072-c93d-4dc5-8eed-77b350c7533c";
        Jar jarToSave = new Jar()
                .setJarName("name")
                .setBudgetId(budgetId)
                .setCapacity(5.0);
        Jar savedJar = new Jar()
                .setJarName("name")
                .setBudgetId(budgetId)
                .setCapacity(5.0)
                .setCurrentAmount(0.0)
                .setId(UUID.randomUUID().toString());
        when(budgetRepository.findById(budgetId))
                .thenReturn(Optional.of(new Budget().setId(budgetId)));
        when(jarRepository.save(refEq(jarToSave, "id"))).thenReturn(savedJar);
    }
    private void mock_jar_update() {
        String budgetIdWithoutJars = "899a073e-12bf-4f27-85e4-3c004985e5b8";
        String budgetId = "899a073e-12bf-4f27-85e4-3c004985e5b8";
        String updatedJarId = "325c033d-f17d-48d2-b75a-e14458200704";
        String missingJarId = "60b4ac5b-8b84-4eb5-a79a-b15af9d0761d";
        Jar jarBeforeModification = new Jar()
                .setId(updatedJarId)
                .setBudgetId(budgetId)
                .setJarName("notModifiedName")
                .setCapacity(5.0)
                .setCurrentAmount(4.0);
        Jar jarAfterModification = new Jar()
                .setId(updatedJarId)
                .setBudgetId(budgetId)
                .setJarName("modifiedName")
                .setCapacity(5.0)
                .setCurrentAmount(4.0);
        when(jarRepository.findByIdAndBudgetId(updatedJarId, budgetId))
                .thenReturn(Optional.of(jarBeforeModification));
        when(jarRepository.findByIdAndBudgetId(missingJarId, budgetIdWithoutJars))
                .thenReturn(Optional.empty());
        when(jarRepository.save(jarAfterModification)).thenReturn(jarAfterModification);
    }
    private void mock_jar_find() {
        String budgetId = "97f459b6-db3a-426a-9b3f-c40d589bc3a2";
        String foundJarId = "8514d8b8-9c87-4909-be0c-bb03c78c0819";
        String missingJarId = "9f769ba3-8b72-4413-9709-f3c3394023eb";
        Jar jarToFind = new Jar()
                .setId(foundJarId)
                .setBudgetId(budgetId)
                .setCapacity(3.0)
                .setCurrentAmount(2.0)
                .setJarName("foundJar");
        when(jarRepository.findByIdAndBudgetId(foundJarId, budgetId))
                .thenReturn(Optional.of(jarToFind));
        when(jarRepository.findByIdAndBudgetId(eq(missingJarId), any()))
                .thenReturn(Optional.empty());
    }
    private void mock_jar_find_all() {
        String budgetIdWithTwoJars = "bb973af2-1147-429b-9379-856a5ede2f60";
        String budgetIdWithoutJars = "25372644-0c05-4ca7-abda-5ec08f0391b3";
        Jar firstJar = new Jar()
                .setId("afb2ab6f-7c0d-4ce7-8130-76efea5adc6b")
                .setBudgetId(budgetIdWithTwoJars)
                .setJarName("name1")
                .setCurrentAmount(0.0)
                .setCapacity(3.0);
        Jar secondJar = new Jar()
                .setId("b3e66a15-09e5-4a32-b9ec-d8c902bae0ea")
                .setBudgetId(budgetIdWithTwoJars)
                .setJarName("name2")
                .setCurrentAmount(0.0)
                .setCapacity(3.0);
        ArrayList<Jar> list = new ArrayList<>(2);
        list.add(firstJar);
        list.add(secondJar);

        Pageable pageable = PageRequest.of(0, 100);

        Page<Jar> pageWithoutJars = new PageImpl<>(Collections.emptyList(), pageable, 0);
        Page<Jar> pageWithJars = new PageImpl<>(list, pageable, 2);

        when(jarRepository.findAllByBudgetId(budgetIdWithTwoJars, pageable))
                .thenReturn(pageWithJars);
        when(jarRepository.findAllByBudgetId(budgetIdWithoutJars, pageable))
                .thenReturn(pageWithoutJars);
    }
    private void mock_budget_find() {
        String familyId = "1d6030f4-9051-4d1b-8b77-4fecd1ab9a52";
        String missingFamilyId = "b0492093-d920-492c-b5de-6e4046962410";
        Budget foundBudget = new Budget()
                .setId("cee8d4bd-e787-4912-91ed-57400cf90892")
                .setFamilyId(familyId)
                .setMaxJars(6L);
        when(budgetRepository.findByFamilyId(familyId))
                .thenReturn(Optional.of(foundBudget));
        when(budgetRepository.findByFamilyId(missingFamilyId))
                .thenReturn(Optional.empty());
    }
    private void mock_budget_save() {
        Budget failureCase = new Budget()
                .setFamilyId("c2d8fd47-75ce-4797-9512-55d73dbeb015")
                .setMaxJars(8L);
        doThrow(FamilyConflictException.class).when(budgetRepository)
                .save(refEq(failureCase, "id"));

        String familyId = "6537138e-1056-45be-bf24-efadbedb428b";
        Budget budgetToSave = new Budget()
                .setFamilyId(familyId)
                .setMaxJars(6L);
        Budget savedBudget = new Budget()
                .setId(UUID.randomUUID().toString())
                .setFamilyId(familyId)
                .setMaxJars(6L);
        when(budgetRepository.save(refEq(budgetToSave, "id"))).thenReturn(savedBudget);
    }
    private void mock_expense_delete() {
        String budgetId = "19e8147b-f6cb-46fa-b1d4-a0cb1ead4a08";
        String deletedExpenseId = "445598c4-480a-452e-9493-8bc7ba709858";
        String missingExpenseId = "570df03f-a98e-4752-bdab-3f7fa67e7945";
        when(budgetRepository.findById(budgetId))
                .thenReturn(Optional.of(new Budget().setId(budgetId)));

        doNothing().when(expenseRepository)
                .deleteByIdAndBudgetId(deletedExpenseId, budgetId);
        when(expenseRepository.findByIdAndBudgetId(deletedExpenseId, budgetId))
                .thenReturn(Optional.of(new Expense()));

        when(expenseRepository.findByIdAndBudgetId(missingExpenseId, budgetId))
                .thenReturn(Optional.empty());
    }
    private void mock_expense_save() {
        String budgetId = "9ab79704-6682-4647-ade6-ac03aaaad427";
        Expense expenseToSave = new Expense()
                .setBudgetId(budgetId)
                .setAmount(5.0)
                .setComment("comment");
        Expense savedExpense = new Expense()
                .setBudgetId(budgetId)
                .setAmount(5.0)
                .setComment("comment")
                .setId(UUID.randomUUID().toString())
                .setCreated(Instant.now());
        when(expenseRepository.save(refEq(expenseToSave, "id", "created")))
                .thenReturn(savedExpense);
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(new Budget()));
    }
    private void mock_expense_find_all() {
        String budgetId = "613c436d-ca18-4f31-9088-90efb19efd54";
        String budgetIdWithoutExpenses = "80adeba9-8ed6-4207-be1e-a1019439c0b5";
        Expense firstExpense = new Expense()
                .setId("52dc50fd-1dd1-4e62-bbab-2485f22f28ce")
                .setBudgetId(budgetId)
                .setAmount(3.0)
                .setComment("test_comments_1")
                .setCreated(Instant.parse("2019-06-16T10:22:54.246625Z"));
        Expense secondExpense = new Expense()
                .setId("896ffae8-0a10-46e1-933a-927a417cf447")
                .setBudgetId(budgetId)
                .setAmount(4.0)
                .setComment("test_comments_2")
                .setCreated(Instant.parse("2019-06-16T10:28:23.053553Z"));
        ArrayList<Expense> expenseList = new ArrayList<>(2);
        expenseList.add(firstExpense);
        expenseList.add(secondExpense);
        Pageable pageable = PageRequest.of(0, 100);
        Page<Expense> page = new PageImpl<>(expenseList, pageable, 2);
        Page<Expense> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(budgetRepository.findById(budgetId))
                .thenReturn(Optional.of(new Budget().setId(budgetId)));
        when(budgetRepository.findById(budgetIdWithoutExpenses))
                .thenReturn(Optional.of(new Budget().setId(budgetIdWithoutExpenses)));

        when(expenseRepository.findAllByBudgetId(budgetId, pageable))
                .thenReturn(page);
        when(expenseRepository.findAllByBudgetId(budgetIdWithoutExpenses, pageable)).thenReturn(emptyPage);
    }
}
