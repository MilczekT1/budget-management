package pl.konradboniecki.budget.budgetmanagement.feature.budget;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Accessors(chain = true)
@Document("budget")
public class Budget {

    @Id
    @Indexed(unique = true)
    private String id;
    @Indexed(unique = true)
    private String familyId;
    private Long maxJars;

    public Budget() {
        setMaxJars(6L);
    }
}


