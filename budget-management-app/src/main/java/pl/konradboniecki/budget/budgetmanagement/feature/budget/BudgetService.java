package pl.konradboniecki.budget.budgetmanagement.feature.budget;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.budgetmanagement.exception.BudgetNotFoundException;
import pl.konradboniecki.budget.budgetmanagement.exception.FamilyConflictException;
import pl.konradboniecki.budget.openapi.dto.model.OASBudget;
import pl.konradboniecki.budget.openapi.dto.model.OASBudgetCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedBudget;
import pl.konradboniecki.chassis.exceptions.BadRequestException;
import pl.konradboniecki.chassis.exceptions.InternalServerErrorException;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    public OASBudget findByOrThrow(String id, String idType) {
        switch (idType) {
            case "id":
                return budgetMapper.toOASBudget(findByIdOrThrow(id));
            case "family":
                return budgetMapper.toOASBudget(findByFamilyIdOrThrow(id));
            default:
                throw new BadRequestException("Invalid argument idType=" + idType + ", it should be \"id\" or \"family\"");
        }
    }

    private Budget findByIdOrThrow(String id) {
        Optional<Budget> budget = budgetRepository.findById(id);
        if (budget.isPresent()) {
            return budget.get();
        } else {
            throw new BudgetNotFoundException("Budget with id: " + id + " not found.");
        }
    }

    private Budget findByFamilyIdOrThrow(String familyId) {
        Optional<Budget> budget = budgetRepository.findByFamilyId(familyId);
        if (budget.isPresent()) {
            return budget.get();
        } else {
            throw new BudgetNotFoundException("Budget not found for family with id: " + familyId);
        }
    }

    public OASCreatedBudget saveBudget(OASBudgetCreation budgetCreation) {
        Budget budget = budgetMapper.toBudget(budgetCreation);
        checkIfBudgetIsAlreadyAssignedToFamily(budget);
        try {
            budget.setId(UUID.randomUUID().toString());
            Budget savedBudget = budgetRepository.save(budget);
            return budgetMapper.toOASCreatedBudget(savedBudget);
        } catch (Exception e) {
            log.error("Failed to save Budget: " + budget);
            throw new InternalServerErrorException("Unexpected error occurred.", e);
        }
    }

    public void deleteBudget(String id) {
        findByIdOrThrow(id);
        budgetRepository.deleteById(id);
    }

    private void checkIfBudgetIsAlreadyAssignedToFamily(Budget budget) {
        if (budgetRepository.findByFamilyId(budget.getFamilyId()).isPresent()) {
            log.error("Failed to create budget. Family with id: {}, already had a budget.", budget.getFamilyId());
            throw new FamilyConflictException("Conflict during budget creation: invalid family");
        }
    }
}
