package pl.konradboniecki.budget.budgetmanagement.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.konradboniecki.budget.budgetmanagement.feature.jar.JarService;
import pl.konradboniecki.budget.openapi.api.JarManagementApi;
import pl.konradboniecki.budget.openapi.dto.model.*;

import static pl.konradboniecki.budget.budgetmanagement.controller.JarController.BASE_PATH;

@AllArgsConstructor
@RestController
@RequestMapping(BASE_PATH)
public class JarController implements JarManagementApi {
    public static final String BASE_PATH = "/api/budget-mgt/v1";

    private final JarService jarService;

    @Override
    public ResponseEntity<OASJar> findJar(String budgetId, String jarId) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jarService.findJar(jarId, budgetId));
    }

    @Override
    public ResponseEntity<OASJarPage> findJars(String budgetId, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        OASJarPage jarPage = jarService.findAllJarsByBudgetId(budgetId, pageable);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jarPage);
    }

    @Override
    public ResponseEntity<OASCreatedJar> createJar(String budgetId, OASJarCreation oaSJarCreation) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jarService.saveJar(oaSJarCreation, budgetId));
    }

    @Override
    public ResponseEntity<OASJar> modifyJar(String budgetId, String jarId, OASJarModification oaSJarModification) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jarService.updateJar(jarId, budgetId, oaSJarModification));
    }

    @Override
    public ResponseEntity<Void> deleteJar(String budgetId, String jarId) {
        jarService.removeJarFromBudgetOrThrow(jarId, budgetId);
        return ResponseEntity
                .noContent()
                .build();
    }
}
