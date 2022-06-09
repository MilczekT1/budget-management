package pl.konradboniecki.budget.budgetmanagement.feature.expense;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@Document("expense")
public class Expense {

    @Id
    @Indexed(unique = true)
    private String id;
    @Indexed(unique = true)
    private String budgetId;
    private Double amount;
    private String comment;
    private Instant created;

    public Expense mergeWith(@NonNull Expense secondExpense) {
        if (secondExpense.getId() != null)
            setId(secondExpense.getId());
        if (secondExpense.getBudgetId() != null)
            setBudgetId(secondExpense.getBudgetId());
        if (secondExpense.getAmount() != null)
            setAmount(secondExpense.getAmount());
        if (!StringUtils.isEmpty(secondExpense.getComment()))
            setComment(secondExpense.getComment());
        if (secondExpense.getCreated() != null)
            setCreated(secondExpense.getCreated());
        return this;
    }
}
