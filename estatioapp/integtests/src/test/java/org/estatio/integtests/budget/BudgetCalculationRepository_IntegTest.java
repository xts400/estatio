package org.estatio.integtests.budget;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budget.BudgetRepository;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculation;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationRepository;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationService;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;
import org.estatio.dom.budgeting.budgetcalculation.Status;
import org.estatio.dom.budgeting.budgetitem.BudgetItem;
import org.estatio.dom.budgeting.keyitem.KeyItem;
import org.estatio.dom.budgeting.partioning.PartitionItem;
import org.estatio.dom.budgeting.partioning.PartitionItemRepository;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.charge.ChargeRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertyForOxfGb;
import org.estatio.fixture.budget.BudgetsForOxf;
import org.estatio.fixture.budget.PartitionItemsForOxf;
import org.estatio.fixture.charge.ChargeRefData;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetCalculationRepository_IntegTest extends EstatioIntegrationTest {

    @Inject
    BudgetCalculationRepository budgetCalculationRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    BudgetRepository budgetRepository;

    @Inject
    PartitionItemRepository partitionItemRepository;

    @Inject
    ChargeRepository chargeRepository;

    @Inject
    BudgetCalculationService budgetCalculationService;

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new PartitionItemsForOxf());
            }
        });
    }

    public static class FindUnique extends BudgetCalculationRepository_IntegTest {

        @Test
        public void happyCase() throws Exception {
            // given
            PartitionItem partitionItem = partitionItemRepository.allPartitionItems().get(0);
            KeyItem keyItem = partitionItem.getKeyTable().getItems().first();
            BudgetItem budgetItem = budgetRepository.allBudgets().get(0).getItems().first();
            BudgetCalculation newBudgetCalculation = budgetCalculationRepository.createBudgetCalculation(budgetItem, partitionItem, keyItem, BigDecimal.ZERO, BudgetCalculationType.BUDGETED);

            // when
            BudgetCalculation budgetCalculation = budgetCalculationRepository.findUnique(budgetItem, partitionItem, keyItem, BudgetCalculationType.BUDGETED);

            // then
            assertThat(budgetCalculation).isEqualTo(newBudgetCalculation);
        }

    }

    public static class FindByPartitionItem extends BudgetCalculationRepository_IntegTest {

        @Test
        public void happyCase() throws Exception {
            // given
            PartitionItem partitionItem = partitionItemRepository.allPartitionItems().get(0);
            KeyItem keyItem = partitionItem.getKeyTable().getItems().first();
            BudgetItem budgetItem = budgetRepository.allBudgets().get(0).getItems().first();
            BudgetCalculation newBudgetCalculation = budgetCalculationRepository.createBudgetCalculation(budgetItem, partitionItem, keyItem, BigDecimal.ZERO, BudgetCalculationType.BUDGETED);

            // when
            List<BudgetCalculation> budgetCalculations = budgetCalculationRepository.findByPartitionItem(partitionItem);

            // then
            assertThat(budgetCalculations.size()).isEqualTo(1);
            assertThat(budgetCalculations.get(0)).isEqualTo(newBudgetCalculation);

        }

    }

    public static class FindByBudgetAndCharge extends BudgetCalculationRepository_IntegTest {

        @Test
        @Ignore // TODO !!
        public void happyCase() throws Exception {

            // given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            Charge charge = chargeRepository.findByReference(ChargeRefData.GB_SERVICE_CHARGE);
            Charge chargeNotToBeFound = chargeRepository.findByReference(ChargeRefData.GB_INCOMING_CHARGE_1);
            budgetCalculationService.calculatePersistedCalculations(budget);

            // when
            List<BudgetCalculation> budgetCalculationsForCharge = budgetCalculationRepository.findByBudgetAndCharge(budget, charge);

            // then
            assertThat(budgetCalculationsForCharge.size()).isEqualTo(75);

            // and when
            budgetCalculationsForCharge = budgetCalculationRepository.findByBudgetAndCharge(budget, chargeNotToBeFound);

            // then
            assertThat(budgetCalculationsForCharge.size()).isEqualTo(0);

        }
    }

    public static class FindByBudgetAndStatus extends BudgetCalculationRepository_IntegTest {

        @Test
        public void happyCase() throws Exception {

            // given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            budgetCalculationService.calculatePersistedCalculations(budget);

            // when
            List<BudgetCalculation> budgetCalculationsForAllocationOfType = budgetCalculationRepository.findByBudgetAndStatus(budget, Status.NEW);

            // then
            assertThat(budgetCalculationsForAllocationOfType.size()).isEqualTo(75);

            // and when
            budgetCalculationsForAllocationOfType = budgetCalculationRepository.findByBudgetAndStatus(budget, Status.ASSIGNED);

            // then
            assertThat(budgetCalculationsForAllocationOfType.size()).isEqualTo(0);

        }
    }

    public static class FindByBudgetAndUnitAndInvoiceChargeAndType extends BudgetCalculationRepository_IntegTest {

        @Test
        public void happyCase() throws Exception {

            // given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            PartitionItem partitionItem = partitionItemRepository.findByBudgetItem(budget.getItems().first()).get(0);
            budgetCalculationService.calculatePersistedCalculations(budget);

            // when
            List<BudgetCalculation> budgetCalculations = budgetCalculationRepository.findByBudgetAndUnitAndInvoiceChargeAndType(budget, property.getUnits().first(), partitionItem.getInvoiceCharge(), BudgetCalculationType.BUDGETED);

            // then
            assertThat(budgetCalculations.size()).isEqualTo(3);

        }
    }

    public static class FindByBudgetAndUnitAndInvoiceChargeAndIncomingChargeAndType extends
            BudgetCalculationRepository_IntegTest {

        @Test
        public void happyCase() throws Exception {

            // given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            PartitionItem partitionItem = partitionItemRepository.findByBudgetItem(budget.getItems().first()).get(0);
            budgetCalculationService.calculatePersistedCalculations(budget);

            // when
            List<BudgetCalculation> budgetCalculations = budgetCalculationRepository.findByBudgetAndUnitAndInvoiceChargeAndIncomingChargeAndType(budget, property.getUnits().first(), partitionItem.getInvoiceCharge(), partitionItem.getIncomingCharge(), BudgetCalculationType.BUDGETED);

            // then
            assertThat(budgetCalculations.size()).isEqualTo(1);

        }
    }

}
