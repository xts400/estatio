/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.dom.lease.invoicing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import org.incode.module.base.dom.valuetypes.LocalDateInterval;

import org.estatio.agreement.dom.AgreementRoleRepository;
import org.estatio.agreement.dom.AgreementRoleType;
import org.estatio.agreement.dom.AgreementRoleTypeRepository;
import org.estatio.agreement.dom.AgreementTypeRepository;
import org.estatio.dom.appsettings.LeaseInvoicingSettingsService;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.invoice.InvoiceRunType;
import org.estatio.dom.invoice.InvoicingInterval;
import org.estatio.dom.lease.InvoicingFrequency;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.LeaseItem;
import org.estatio.dom.lease.LeaseItemType;
import org.estatio.dom.lease.LeaseTerm;
import org.estatio.dom.lease.LeaseTermForDeposit;
import org.estatio.dom.lease.LeaseTermForTesting;
import org.estatio.dom.lease.LeaseTermValueType;
import org.estatio.dom.tax.Tax;
import org.estatio.dom.tax.TaxRate;
import org.estatio.dom.tax.TaxRateRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class InvoiceCalculationService_Test {

    public static abstract class CalculateDueDateRange extends InvoiceCalculationService_Test {

        @Rule
        public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

        InvoiceCalculationService ic;

        List<InvoiceCalculationService.CalculationResult> calculateDueDateRange(
                final LeaseTerm leaseTerm,
                final LocalDate startDueDate,
                final LocalDate nextDueDate,
                final Double... values
        ) {
            return calculateDueDateRange(leaseTerm, startDueDate, nextDueDate, leaseTerm.getLeaseItem().getInvoicingFrequency(), values);
        }

        List<InvoiceCalculationService.CalculationResult> calculateDueDateRange(
                final LeaseTerm leaseTerm,
                final LocalDate startDueDate,
                final LocalDate nextDueDate,
                final InvoicingFrequency invoicingFrequency,
                final Double... values
        ) {
            List<InvoiceCalculationService.CalculationResult> results = ic.calculateDueDateRange(
                    leaseTerm,
                    InvoiceCalculationParameters.builder()
                            .leaseTerm(leaseTerm)
                            .invoiceRunType(InvoiceRunType.NORMAL_RUN)
                            .invoiceDueDate(startDueDate)
                            .startDueDate(startDueDate)
                            .nextDueDate(nextDueDate == null ? startDueDate.plusDays(1) : nextDueDate).build());
            for (int i = 0; i < results.size(); i++) {
                assertThat(results.get(i).value().subtract(results.get(i).mockValue())).isEqualTo(new BigDecimal(values[i]).setScale(2, RoundingMode.HALF_UP));
            }
            return results;
        }

        static final LocalDate LEASE_START_DATE = new LocalDate(2011, 11, 1);
        static final LocalDate LEASE_END_DATE = new LocalDate(2011, 11, 1).plusYears(10).minusDays(1);

        Lease lease;
        LeaseItem leaseItem;
        LeaseTermForTesting leaseTerm;

        Tax tax;
        TaxRate taxRate;
        Charge charge;

        AgreementRoleType artLandlord;
        AgreementRoleType artTenant;

        @Mock
        TaxRateRepository mockTaxRateRepository;

        @Mock
        AgreementRoleRepository mockAgreementRoleRepository;

        @Mock
        AgreementRoleTypeRepository mockAgreementRoleTypeRepository;

        @Mock
        AgreementTypeRepository mockAgreementTypeRepository;

        @Mock
        LeaseInvoicingSettingsService mockSettings;

        InvoiceItemForLease invoiceItemForLease;

        @Before
        public void setup() {
            artLandlord = new AgreementRoleType();
            artLandlord.setTitle("Landlord");

            artTenant = new AgreementRoleType();
            artTenant.setTitle("Tenant");

            context.checking(new Expectations() {
                {
                    allowing(mockAgreementRoleTypeRepository).findByTitle("Landlord");
                    will(returnValue(artLandlord));
                    allowing(mockAgreementRoleTypeRepository).findByTitle("Tenant");
                    will(returnValue(artTenant));
                }
            });

            lease = new Lease();
            lease.agreementRoleRepository = mockAgreementRoleRepository;
            lease.setStartDate(LEASE_START_DATE);
            lease.setEndDate(LEASE_END_DATE);

            leaseItem = new LeaseItem();
            leaseItem.setStartDate(LEASE_START_DATE);
            leaseItem.setInvoicingFrequency(InvoicingFrequency.QUARTERLY_IN_ADVANCE);
            leaseItem.setLease(lease);

            lease.getItems().add(leaseItem);

            leaseTerm = new LeaseTermForTesting();
            leaseTerm.setLeaseItem(leaseItem);

            leaseItem.getTerms().add(leaseTerm);

            tax = new Tax();
            tax.taxRateRepository = mockTaxRateRepository;
            tax.setReference("VAT");

            taxRate = new TaxRate();
            taxRate.setPercentage(BigDecimal.valueOf(21));

            charge = new Charge();
            charge.setReference("IT_RENT");
            charge.setTax(tax);

            invoiceItemForLease = new InvoiceItemForLease();
            invoiceItemForLease.setLeaseTerm(leaseTerm);

            invoiceItemForLease.agreementRoleTypeRepository = mockAgreementRoleTypeRepository;
            invoiceItemForLease.agreementTypeRepository = mockAgreementTypeRepository;

            ic = new InvoiceCalculationService();
            ic.leaseInvoicingSettingsService = mockSettings;
        }

        public static class Regular extends CalculateDueDateRange {

            @Before
            public void setup() {
                super.setup();

                context.checking(new Expectations() {
                    {
                        allowing(mockSettings).fetchEpochDate();
                        will(returnValue(new LocalDate(1980, 1, 1)));
                    }
                });
            }

            @Test
            public void testCalculateFullQuarter() {
                leaseTerm.setStartDate(LEASE_START_DATE);
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), null, 5000.00);
            }

            @Test
            public void testCalculateExactPeriod() {
                leaseTerm.setStartDate(new LocalDate(2012, 1, 1));
                leaseTerm.setEndDate(new LocalDate(2012, 3, 31));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), null, 5000.00);
            }

            @Test
            public void testCalculateSingleMonth() {
                leaseTerm.setStartDate(new LocalDate(2012, 2, 1));
                leaseTerm.setEndDate(new LocalDate(2012, 2, 29));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), null, 1593.41);
            }

            @Test
            public void testCalculateWithFrequency() {
                leaseTerm.setStartDate(new LocalDate(2012, 2, 1));
                leaseTerm.setEndDate(new LocalDate(2012, 2, 29));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), new LocalDate(2012, 3, 1), 1593.41);
            }

            @Test
            public void testCalculateNothing() {
                leaseTerm.setStartDate(new LocalDate(2013, 1, 1));
                leaseTerm.setEndDate(new LocalDate(2013, 3, 1));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), null, 0.00);
            }

            @Test
            public void testDateAfterTerminationDate() {
                lease.terminate(new LocalDate(2013, 12, 31));
                LeaseTermForTesting t2 = new LeaseTermForTesting(leaseItem, new LocalDate(2014, 1, 1), null, new BigDecimal("20000.00"));
                calculateDueDateRange(t2, new LocalDate(2014, 1, 1), null, 0.00);
            }

            @Test
            public void testWithNonMatchingStartDate() {
                leaseTerm.setStartDate(new LocalDate(2013, 1, 1));
                leaseTerm.setEndDate(new LocalDate(2013, 3, 1));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 2), null);
            }

            @Test
            public void testwithTerminationDate() {
                leaseTerm.setStartDate(new LocalDate(2012, 1, 1));
                leaseTerm.setEndDate(new LocalDate(2012, 3, 31));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                lease.setTenancyEndDate(new LocalDate(2012, 1, 31));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), null, 1703.30);
                calculateDueDateRange(leaseTerm, new LocalDate(2014, 1, 1), null, 0.00);
            }

            @Test
            public void testFuture() {
                leaseItem.setCharge(charge);
                leaseTerm.setStartDate(new LocalDate(2014, 1, 1));
                leaseTerm.setEndDate(new LocalDate(2014, 12, 31));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                leaseTerm.setAdjustedValue(BigDecimal.valueOf(22000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), new LocalDate(2014, 1, 1), 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00);
            }

            @Test
            public void testCalculateWithFrequencyDifferentEndDate() {
                leaseTerm.setStartDate(new LocalDate(2012, 2, 1));
                leaseTerm.setEndDate(new LocalDate(2012, 2, 29));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), new LocalDate(2012, 2, 28), 1593.41);
            }

            @Test
            public void testWithYearlyInvoicingFrequency() {
                leaseTerm.setStartDate(new LocalDate(2012, 2, 1));
                leaseTerm.setEndDate(new LocalDate(2013, 1, 1));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), new LocalDate(2013, 1, 1), InvoicingFrequency.YEARLY_IN_ARREARS,
                        3296.70, 5000.00, 5000.00, 5000.00);
            }

            @Test
            public void testFullCalculationResults() {
                leaseTerm.setStartDate(new LocalDate(2012, 2, 1));
                leaseTerm.setEndDate(new LocalDate(2013, 1, 31));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, leaseTerm.getStartDate(), leaseTerm.getEndDate(),
                        5000.00, 5000.00, 5000.00, 1722.22);
                // TODO: Since 2012 is a leap year, the keySum of the invoices is greater
                // than the value of the term.....
            }

            @Test
            public void testCalculateAfterEndDate() {
                leaseTerm.setStartDate(new LocalDate(2014, 1, 1));
                leaseTerm.setEndDate(new LocalDate(2014, 12, 31));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                lease.setTenancyEndDate(new LocalDate(2013, 12, 31));
                calculateDueDateRange(leaseTerm, new LocalDate(2014, 1, 1), new LocalDate(2014, 7, 1),
                        0.00, 0.00);
            }

            List<InvoiceCalculationService.CalculationResult> calculateDueDateRange(
                    final LeaseTerm leaseTerm,
                    final LocalDate startDueDate,
                    final LocalDate nextDueDate,
                    final InvoicingFrequency invoicingFrequency,
                    final Double... values
            ) {
                final List<InvoiceCalculationService.CalculationResult> calculationResults = super.calculateDueDateRange(leaseTerm, startDueDate, nextDueDate, invoicingFrequency, values);

                // REVIEW: this assertion done here because fails in some cases for the epoch case, below
                assertThat(calculationResults).hasSize(values.length);

                return calculationResults;
            }

        }

        public static class WithEpochDate extends CalculateDueDateRange {

            @Before
            public void setup() {
                super.setup();

                context.checking(new Expectations() {
                    {
                        allowing(mockSettings).fetchEpochDate();
                        will(returnValue(new LocalDate(2013, 1, 1)));
                    }
                });
            }

            @Test
            public void testFuture() {
                leaseItem.setCharge(charge);
                leaseTerm.setStartDate(new LocalDate(2014, 1, 1));
                leaseTerm.setEndDate(new LocalDate(2014, 12, 31));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                leaseTerm.setAdjustedValue(BigDecimal.valueOf(22000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), new LocalDate(2014, 1, 1),
                        0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00);
            }

            @Test
            public void testWithYearlyInvoicingFrequency() {
                leaseTerm.setStartDate(new LocalDate(2012, 2, 1));
                leaseTerm.setEndDate(new LocalDate(2013, 1, 1));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), new LocalDate(2012, 1, 1), InvoicingFrequency.YEARLY_IN_ARREARS,
                        3296.70, 5000.00, 5000.00, 5000.00);

            }

            @Test
            public void testFullCalculationResults() {
                leaseTerm.setStartDate(new LocalDate(2012, 2, 1));
                leaseTerm.setEndDate(new LocalDate(2013, 1, 31));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, leaseTerm.getStartDate(), leaseTerm.getEndDate(),
                        0.00, 0.00, 0.00, 1722.22);
                // TODO: Since 2012 is a leap year, the keySum of the invoices is greater
                // than the value of the term.....
            }

        }

        public static class WithValueTypeFixed extends CalculateDueDateRange {

            LeaseTerm leaseTermWithFixedValueType;

            @Before
            public void setup() {
                super.setup();

                leaseTerm.setLeaseTermValueType(LeaseTermValueType.FIXED);

                context.checking(new Expectations() {
                    {
                        allowing(mockSettings).fetchEpochDate();
                        will(returnValue(new LocalDate(1980, 1, 1)));
                    }
                });

            }

            @Test
            public void testCalculateFullQuarter() {
                leaseTerm.setStartDate(LEASE_START_DATE);
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, new LocalDate(2012, 1, 1), null, 20000.00);
            }

            @Test
            public void testFullCalculationResults() {
                leaseTerm.setStartDate(new LocalDate(2012, 2, 1));
                leaseTerm.setEndDate(new LocalDate(2013, 1, 31));
                leaseTerm.setValue(BigDecimal.valueOf(20000));
                calculateDueDateRange(leaseTerm, leaseTerm.getStartDate(), leaseTerm.getEndDate(),
                        20000.00, 20000.00, 20000.00, 20000.00);
            }

        }

    }

    public static class CalculateDateRange extends InvoiceCalculationService_Test {

        @Rule
        public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

        InvoiceCalculationService ic;

        @Mock
        LeaseInvoicingSettingsService mockSettings;

        @Before
        public void setUp() throws Exception {
            ic = new InvoiceCalculationService();
            ic.leaseInvoicingSettingsService = mockSettings;

            context.checking(new Expectations() {
                {
                    allowing(mockSettings).fetchEpochDate();
                    will(returnValue(new LocalDate(2013, 1, 1)));
                }
            });

        }

        @Test
        public void testDateRange() throws Exception {

            tester(InvoicingFrequency.QUARTERLY_IN_ADVANCE, "2014-02-01/2015-02-01", 10000.00, "2014-01-01/2015-01-01", "2015-01-01", 1638.89, 2500.00, 2500.00, 2500.00);
            tester(InvoicingFrequency.QUARTERLY_IN_ADVANCE, "2014-02-01/2015-02-01", 10000.00, "2015-01-01/2016-01-01", "2015-01-01", 861.11, 0.00, 0.00, 0.00);
            tester(InvoicingFrequency.QUARTERLY_IN_ARREARS, "2014-02-01/2015-02-01", 10000.00, "2015-01-01/2016-01-01", "2015-01-01", 861.11, 0.00, 0.00, 0.00);

            tester(InvoicingFrequency.MONTHLY_IN_ADVANCE, "2014-02-01/2015-02-01", 10000.00, "2014-02-01/2015-02-01", "2015-01-01", 833.33, 833.33, 833.33, 833.33, 833.33, 833.33, 833.33, 833.33, 833.33, 833.33, 833.33, 833.33);
        }

        @Test // When EST-112 is solved this one should break and can be removed
        public void testDateRangeWithZeroDayInterval() throws Exception {
            tester(InvoicingFrequency.QUARTERLY_IN_ADVANCE, "1999-09-01/2015-02-01", 10000.00, "1999-09-01/2000-02-01", "2000-01-01", 0.00, 2500.00);
        }

        private void tester(final InvoicingFrequency invoicingFrequency, final String termInterval, final double termValue, final String caluculationInterval, final String dueDate, final Double... results) {
            Lease lease = new Lease();
            LeaseItem item = new LeaseItem(lease, invoicingFrequency);
            LeaseTerm term = new LeaseTermForTesting(item, LocalDateInterval.parseString(termInterval), new BigDecimal(termValue));

            final List<InvoiceCalculationService.CalculationResult> calculationResults = ic.calculateDateRange(term, LocalDateInterval.parseString(caluculationInterval), new LocalDate(dueDate));

            compareResults(calculationResults, results);
        }

        private void compareResults(List<InvoiceCalculationService.CalculationResult> results, Double... value) {
            assertThat(results).hasSize(value.length);
            Arrays.asList(value);

            for (int i = 0; i < results.size(); i++) {
                assertThat(results.get(i).value()).isEqualTo(BigDecimal.valueOf(value[i]).setScale(2));
            }

        }

    }

    public static class CalculateTermTest extends InvoiceCalculationService_Test {

        @Rule
        public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

        InvoiceCalculationService ic;
        LocalDate epochDate = new LocalDate(2013, 1, 1);

        @Mock
        LeaseInvoicingSettingsService mockSettings;

        @Before
        public void setUp() throws Exception {
            ic = new InvoiceCalculationService();
            ic.leaseInvoicingSettingsService = mockSettings;

            context.checking(new Expectations() {
                {
                    allowing(mockSettings).fetchEpochDate();
                    will(returnValue(epochDate));
                }
            });

        }

        @Test
        public void depositTerm_Should_Have_No_MockValue() throws Exception {

            //given
            LocalDate startDateBeforeEpochDate = new LocalDate(2000, 1, 1);
            LocalDate endDateBeforeEpochDate = new LocalDate(2000, 3,31);
            LocalDate dueDateBeforeEpochDate = new LocalDate(2000, 4, 8);

            LeaseItem depositItem = new LeaseItem();
            depositItem.setType(LeaseItemType.DEPOSIT);
            depositItem.setInvoicingFrequency(InvoicingFrequency.QUARTERLY_IN_ADVANCE);
            LeaseTermForDeposit depositTerm = new LeaseTermForDeposit(){
                @Override
                public LocalDateInterval getEffectiveInterval() {
                    return new LocalDateInterval(startDateBeforeEpochDate, endDateBeforeEpochDate);
                }
                @Override
                public BigDecimal valueForDate(final LocalDate dueDate){
                    return new BigDecimal("1000.00");
                }
            };
            depositTerm.setLeaseItem(depositItem);
            InvoicingInterval invoicingInterval = new InvoicingInterval(
                    new LocalDateInterval(startDateBeforeEpochDate, endDateBeforeEpochDate),
                    dueDateBeforeEpochDate);
            List<InvoicingInterval> intervals = Arrays.asList(invoicingInterval);
            LocalDate dueDateForCalculation = dueDateBeforeEpochDate;

            //when
            List<InvoiceCalculationService.CalculationResult> calculationResults = ic.calculateTerm(depositTerm, intervals, dueDateForCalculation);

            // then
            assertThat(calculationResults.size()).isEqualTo(1);
            assertThat(calculationResults.get(0).mockValue()).isEqualTo(BigDecimal.ZERO.setScale(2));

        }

    }

}