package org.estatio.app.mixins.budgetassignment;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.keytable.FoundationValueType;
import org.estatio.dom.budgeting.keytable.KeyTable;
import org.estatio.dom.budgeting.keytable.KeyTableRepository;
import org.estatio.dom.budgeting.keytable.KeyValueMethod;

@Mixin(method = "exec")
public class Budget_NewKeyTable {

    private final Budget budget;
    public Budget_NewKeyTable(Budget budget){
        this.budget = budget;
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public KeyTable exec(
            final String name,
            final FoundationValueType foundationValueType,
            final KeyValueMethod keyValueMethod
    ) {
        return keyTableRepository.newKeyTable(budget.getPartitioningForBudgeting(), name, foundationValueType, keyValueMethod, 6);
    }

    public String validateExec(
            final String name,
            final FoundationValueType foundationValueType,
            final KeyValueMethod keyValueMethod) {
        return keyTableRepository.validateNewKeyTable(budget.getPartitioningForBudgeting(), name, foundationValueType, keyValueMethod, 6);
    }

    @Inject
    private KeyTableRepository keyTableRepository;

}
