package pl.konradboniecki.budget.budgetmanagement.feature.jar;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.budgetmanagement.exception.BudgetNotFoundException;
import pl.konradboniecki.budget.budgetmanagement.exception.JarCreationException;
import pl.konradboniecki.budget.budgetmanagement.exception.JarNotFoundException;
import pl.konradboniecki.budget.budgetmanagement.feature.budget.BudgetRepository;
import pl.konradboniecki.budget.openapi.dto.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@AllArgsConstructor
@Service
public class JarService {

    private final JarRepository jarRepository;
    private final BudgetRepository budgetRepository;
    private final JarMapper jarMapper;

    public OASJar findJar(String id, String budgetId) {
        Jar jar = findByIdAndBudgetIdOrThrow(id, budgetId);
        return jarMapper.toOASJar(jar);
    }

    private Jar findByIdAndBudgetIdOrThrow(String id, String budgetId) {
        Optional<Jar> jar = jarRepository.findByIdAndBudgetId(id, budgetId);
        if (jar.isPresent()) {
            return jar.get();
        } else {
            throw new JarNotFoundException("Jar with id: " + id + " not found in budget with id: " + budgetId);
        }
    }

    public OASCreatedJar saveJar(OASJarCreation jarCreation, String budgetIdFromPath) {
        Jar jarToSave = jarMapper.toJar(jarCreation);
        checkIfBudgetIdFromPathAndBodyAreConsistent(jarToSave, budgetIdFromPath);
        budgetExistsOrThrow(budgetIdFromPath, "Failed to create jar. Budget not found.");
        jarToSave.setId(UUID.randomUUID().toString());
        Jar savedJar = jarRepository.save(jarToSave);
        return jarMapper.toOASCreatedJar(savedJar);
    }

    public OASJar updateJar(String jarId, String budgetId, OASJarModification jarModification) {
        checkArgument(jarId.equals(jarModification.getId()));
        checkArgument(budgetId.equals(jarModification.getBudgetId()));
        Jar newJar = jarMapper.toJar(jarModification);
        Jar origin = findByIdAndBudgetIdOrThrow(jarId, budgetId);
        Jar result = jarRepository.save(origin.mergeWith(newJar));
        return jarMapper.toOASJar(result);
    }

    public void removeJarFromBudgetOrThrow(String jarId, String budgetId) {
        budgetExistsOrThrow(budgetId, "Failed to delete jar. Budget not found.");
        Long deleted = jarRepository.deleteJarByIdAndBudgetId(jarId, budgetId);
        if (deleted == 0) {
            throw new JarNotFoundException("Jar with id: " + jarId + " not found in budget with id: " + budgetId);
        }
    }

    public void deleteJarByIdOrThrow(String id) {
        try {
            jarRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new JarNotFoundException("Jar with id: " + id + " not found.");
        }
    }

    public OASJarPage findAllJarsByBudgetId(String budgetId, Pageable pageable) {
        Page<Jar> jarPage = jarRepository.findAllByBudgetId(budgetId, pageable);
        List<OASJar> items = jarPage.get()
                .map(jarMapper::toOASJar)
                .collect(Collectors.toList());

        OASPaginationMetadata paginationMetadata = new OASPaginationMetadata()
                .elements(jarPage.getNumberOfElements())
                .pageSize(pageable.getPageSize())
                .page(pageable.getPageNumber())
                .totalPages(jarPage.getTotalPages())
                .totalElements((int) jarPage.getTotalElements());

        return new OASJarPage()
                .items(items)
                .meta(paginationMetadata);
    }

    private void budgetExistsOrThrow(String budgetId, String msg) {
        if (budgetRepository.findById(budgetId).isEmpty()) {
            throw new BudgetNotFoundException(msg);
        }
    }

    private void checkIfBudgetIdFromPathAndBodyAreConsistent(Jar jarFromBody, String budgetIdFromPath) {
        String budgetIdFromBody = jarFromBody.getBudgetId();
        if (!budgetIdFromBody.equals(budgetIdFromPath)) {
            throw new JarCreationException("Budget id in body and path don't match.");
        }
    }
}
