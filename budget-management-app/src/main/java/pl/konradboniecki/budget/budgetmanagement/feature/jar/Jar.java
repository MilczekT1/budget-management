package pl.konradboniecki.budget.budgetmanagement.feature.jar;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Accessors(chain = true)
@Document("jar")
public class Jar {

    @Id
    @Indexed(unique = true)
    private String id;
    @Indexed(unique = true)
    private String budgetId;
    private String jarName;
    private Double currentAmount;
    private Double capacity;
    @Setter(AccessLevel.NONE)
    private String status;

    public Jar() {
        setCapacity(0.0);
        setCurrentAmount(0.0);
    }

    public Jar mergeWith(@NonNull Jar secondJar) {
        if (secondJar.getId() != null)
            setId(secondJar.getId());
        if (secondJar.getBudgetId() != null)
            setBudgetId(secondJar.getBudgetId());
        if (secondJar.getCapacity() != null)
            setCapacity(secondJar.getCapacity());
        if (secondJar.getCurrentAmount() != null)
            setCurrentAmount(secondJar.getCurrentAmount());
        if (!StringUtils.isEmpty(secondJar.getJarName()))
            setJarName(secondJar.getJarName());
        setStatus();
        return this;
    }

    public Jar setCurrentAmount(Double newAmount) {
        this.currentAmount = newAmount;
        setStatus();
        return this;
    }

    public Jar setCapacity(Double newCapacity) {
        this.capacity = newCapacity;
        setStatus();
        return this;
    }

    private Jar setStatus() {
        if (currentAmount == null || capacity == null || capacity <= 0.0 || currentAmount == 0.0) {
            this.status = JarStatus.NOT_STARTED.getStatus();
        } else if (currentAmount < capacity) {
            this.status = JarStatus.IN_PROGRESS.getStatus();
        } else {
            this.status = JarStatus.COMPLETED.getStatus();
        }
        return this;
    }
}
