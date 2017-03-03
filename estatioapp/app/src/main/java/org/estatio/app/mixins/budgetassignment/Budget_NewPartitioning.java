package org.estatio.app.mixins.budgetassignment;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;
import org.estatio.dom.budgeting.partioning.PartitioningRepository;

@Mixin(method = "exec")
public class Budget_NewPartitioning {

    private final Budget budget;
    public Budget_NewPartitioning(Budget budget){
        this.budget = budget;
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
    public Budget exec() {
        partitioningRepository.newPartitioning(budget.getProperty(),budget.getStartDate(), budget.getEndDate(), BudgetCalculationType.ACTUAL);
        return budget;
    }

    public String validateExec(){
        return partitioningRepository.validateNewPartitioning(budget.getProperty(), budget.getStartDate(), budget.getEndDate(), BudgetCalculationType.ACTUAL);
    }

    // TODO !! Not valid anymore ...
    public String disableExec(){
        return partitioningRepository.findByPropertyAndType(budget.getProperty(), BudgetCalculationType.ACTUAL).size()>0 ? "Partitioning for reconciliation already exists" : null;
    }


    @Inject
    private PartitioningRepository partitioningRepository;

}
