package pl.konradboniecki.budget.budgetmanagement.feature.jar;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedJar;
import pl.konradboniecki.budget.openapi.dto.model.OASJar;
import pl.konradboniecki.budget.openapi.dto.model.OASJarCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASJarModification;

@Service
public class JarMapper {

    public Jar toJar(@NonNull OASJarCreation jarCreation) {
        return new Jar()
                .setBudgetId(jarCreation.getBudgetId())
                .setJarName(jarCreation.getJarName())
                .setCapacity(jarCreation.getCapacity())
                .setCurrentAmount(jarCreation.getCurrentAmount());
    }

    public Jar toJar(@NonNull OASJarModification jarModification) {
        return new Jar()
                .setId(jarModification.getId())
                .setBudgetId(jarModification.getBudgetId())
                .setJarName(jarModification.getJarName())
                .setCurrentAmount(jarModification.getCurrentAmount())
                .setCapacity(jarModification.getCapacity());
    }

    public OASJar toOASJar(@NonNull Jar jar) {
        return new OASJar()
                .id(jar.getId())
                .budgetId(jar.getBudgetId())
                .jarName(jar.getJarName())
                .capacity(jar.getCapacity())
                .currentAmount(jar.getCurrentAmount())
                .status(jar.getStatus());
    }

    public OASJarCreation toOASJarCreation(@NonNull Jar jar) {
        return new OASJarCreation()
                .budgetId(jar.getBudgetId())
                .capacity(jar.getCapacity())
                .jarName(jar.getJarName())
                .currentAmount(jar.getCurrentAmount());
    }

    public OASCreatedJar toOASCreatedJar(@NonNull Jar jar) {
        return new OASCreatedJar()
                .id(jar.getId())
                .budgetId(jar.getBudgetId())
                .jarName(jar.getJarName())
                .currentAmount(jar.getCurrentAmount())
                .capacity(jar.getCapacity())
                .status(jar.getStatus());
    }

    public OASJarModification toOASJarModification(@NonNull Jar jar) {
        return new OASJarModification()
                .id(jar.getId())
                .budgetId(jar.getBudgetId())
                .jarName(jar.getJarName())
                .capacity(jar.getCapacity())
                .currentAmount(jar.getCurrentAmount())
                .status(jar.getStatus());
    }
}
