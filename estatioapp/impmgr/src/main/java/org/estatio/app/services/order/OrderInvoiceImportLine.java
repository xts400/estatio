package org.estatio.app.services.order;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.jdo.annotations.Column;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.isisaddons.module.excel.dom.ExcelFixture2;
import org.isisaddons.module.excel.dom.FixtureAwareRowHandler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        objectType = "org.estatio.app.services.order.OrderInvoiceImportLine"
)
@AllArgsConstructor
public class OrderInvoiceImportLine implements FixtureAwareRowHandler<OrderInvoiceImportLine> {

    public String title() {
        return "Order - Invoice Import Line";
    }

    public OrderInvoiceImportLine() {
    }

    @Getter @Setter
    @PropertyLayout(hidden = Where.EVERYWHERE)
    private int rowCounter;
    @Getter @Setter
    @MemberOrder(sequence = "1")
    private String status;
    @Getter @Setter
    @MemberOrder(sequence = "2")
    private String orderNumber;
    @Getter @Setter
    @MemberOrder(sequence = "3")
    private String charge;
    @Getter @Setter
    @MemberOrder(sequence = "4")
    private LocalDate entryDate;
    @Getter @Setter
    @MemberOrder(sequence = "5")
    private LocalDate orderDate;
    @Getter @Setter
    @MemberOrder(sequence = "6")
    private String seller;
    @Getter @Setter
    @MemberOrder(sequence = "7")
    private String orderDescription;
    @Getter @Setter
    @MemberOrder(sequence = "8")
    @Column(scale = 2)
    private BigDecimal netAmount;
    @Getter @Setter
    @MemberOrder(sequence = "9")
    @Column(scale = 2)
    private BigDecimal vatAmount;
    @Getter @Setter
    @MemberOrder(sequence = "10")
    @Column(scale = 2)
    private BigDecimal grossAmount;
    @Getter @Setter
    @MemberOrder(sequence = "11")
    private String orderApprovedBy;
    @Getter @Setter
    @MemberOrder(sequence = "12")
    private String orderApprovedOn;
    @Getter @Setter
    @MemberOrder(sequence = "13")
    private String projectReference;
    @Getter @Setter
    @MemberOrder(sequence = "14")
    private String period;
    @Getter @Setter
    @MemberOrder(sequence = "15")
    private String tax;
    @Getter @Setter
    @MemberOrder(sequence = "16")
    private String invoiceNumber;
    @Getter @Setter
    @MemberOrder(sequence = "17")
    private String invoiceDescription;
    @Getter @Setter
    @MemberOrder(sequence = "18")
    @Column(scale = 2)
    private BigDecimal invoiceNetAmount;
    @Getter @Setter
    @MemberOrder(sequence = "19")
    @Column(scale = 2)
    private BigDecimal invoiceVatAmount;
    @Getter @Setter
    @MemberOrder(sequence = "20")
    @Column(scale = 2)
    private BigDecimal invoiceGrossAmount;
    @Getter @Setter
    @MemberOrder(sequence = "21")
    private String invoiceTax;

    /**
     * To allow for usage within fixture scripts also.
     */
    @Setter
    private FixtureScript.ExecutionContext executionContext;

    /**
     * To allow for usage within fixture scripts also.
     */
    @Setter
    private ExcelFixture2 excelFixture2;

    public OrderInvoiceImportLine handle(final OrderInvoiceImportLine previousRow){

        if (previousRow==null) {
            setRowCounter(1);
        } else {
            setRowCounter(previousRow.getRowCounter() + 1);
            if (getCharge() == null && previousRow.getCharge() != null) {
                setCharge(previousRow.getCharge());
            }
            if (getProjectReference() == null && previousRow.getProjectReference() != null){
                setProjectReference(previousRow.getProjectReference());
            }
            if (getPeriod() == null && previousRow.getPeriod() != null){
                setPeriod(previousRow.getPeriod());
            }
            if (getTax() == null && previousRow.getTax() != null){
                setTax(previousRow.getTax());
            }
        }

        OrderInvoiceImportLine lineItem = null;

        setStatus(validateRow());

        if (entryDate!=null || (invoiceNumber != null && invoiceNumber.matches(".*\\d.*"))) {
            lineItem = new OrderInvoiceImportLine(
                    rowCounter,
                    status,
                    clean(orderNumber),
                    charge,
                    entryDate,
                    orderDate,
                    seller,
                    orderDescription,
                    netAmount!=null ? netAmount.setScale(2, BigDecimal.ROUND_HALF_UP):null,
                    vatAmount!=null ? vatAmount.setScale(2, BigDecimal.ROUND_HALF_UP):null,
                    grossAmountToUse()!=null ? grossAmountToUse().setScale(2, BigDecimal.ROUND_HALF_UP):null,
                    clean(orderApprovedBy),
                    orderApprovedOn,
                    projectReference,
                    period,
                    taxToUse(),
                    clean(invoiceNumber),
                    invoiceDescriptionToUse(),
                    invoiceNetAmountToUse()!=null?invoiceNetAmountToUse().setScale(2, BigDecimal.ROUND_HALF_UP):null,
                    invoiceVatAmountToUse()!=null?invoiceVatAmountToUse().setScale(2, BigDecimal.ROUND_HALF_UP):null,
                    invoiceGrossAmountToUse()!=null?invoiceGrossAmountToUse().setScale(2, BigDecimal.ROUND_HALF_UP):null,
                    invoiceTaxToUse(),
                    this.executionContext,
                    this.excelFixture2
            );
        }

        return lineItem;

    }

    private String clean(final String input){
        if (input==null){
            return null;
        }
        String result = input.trim();
        result = result.replace("Devis n°","");
        result = result.replace("Devis ","");
        result = result.replace("Devis","");
        result = result.replace("Accord ","");
        result = result.replace("Facture n°","");
        result = result.replace("Facture ","");
        result = result.replace("Facture","");
        return result.trim();
    }

    private String convert(final String input){

        if (input==null){
            return null;
        }

        // tax
        String result = input;
        if (input.toLowerCase().equals("tva normale")){
            result = "FRF";
        }
        if (input.toLowerCase().equals("exempt")){
            result = "FRE";
        }
        return result;
    }

    private BigDecimal grossAmountToUse(){
        if (getGrossAmount() != null){
            return getGrossAmount();
        }
        if (getNetAmount()!= null && getVatAmount() != null) {
            return getNetAmount().add(getVatAmount());
        }
        if (getNetAmount()!= null && taxToUse().equals("FRE")){
            return getNetAmount();
        }
        return null;
    }

    private String taxToUse(){
        return getTax() != null ? convert(getTax()) : null;
    }

    private String invoiceTaxToUse(){
        if (getInvoiceNumber()==null){
            return null;
        }
        return getInvoiceTax() != null ? convert(getInvoiceTax()) : taxToUse();
    }

    private BigDecimal invoiceNetAmountToUse(){
        if (getInvoiceNumber()==null){
            return null;
        }
        return getInvoiceNetAmount() != null ? getInvoiceNetAmount() : getNetAmount();
    }

    private BigDecimal invoiceGrossAmountToUse(){
        if (getInvoiceNumber()==null){
            return null;
        }
        return getInvoiceGrossAmount() !=null ? getInvoiceGrossAmount() : grossAmountToUse();
    }

    private BigDecimal invoiceVatAmountToUse(){
        if (getInvoiceNumber()==null){
            return null;
        }
        return getInvoiceVatAmount() != null ? getInvoiceVatAmount() : getVatAmount();
    }

    private String invoiceDescriptionToUse(){
        if (getInvoiceNumber()==null){
            return null;
        }
        return getInvoiceDescription() != null ? getInvoiceDescription() : getOrderDescription();
    }

    private String validateRow(){
        StringBuilder b = new StringBuilder();

        // both order and invoice validation
        if (getCharge()==null || getCharge().equals("")){
            b.append("no charge; ");
        }
        if (getProjectReference()==null || getProjectReference().equals("")){
            b.append("no project reference; ");
        }
        if (getSeller() == null || getSeller().equals("")) {
            b.append("no seller; ");
        }
        if (getPeriod() == null || getPeriod().equals("")) {
            b.append("no period; ");
        }
        // order validation
        if (getEntryDate()!=null) {
            if (getOrderNumber() == null || getOrderNumber().equals("")) {
                b.append("no ordernumber; ");
            }
            if (getVatAmount() == null && taxToUse()!=null && !taxToUse().equals("FRE")) {
                b.append("no vat amount; ");
            }
            if (getNetAmount() == null) {
                b.append("no net amount; ");
            }
            if (grossAmountToUse() == null){
                b.append("no gross amount; ");
            }
            if (getTax() == null || getTax().equals("")) {
                b.append("no tax; ");
            }
        }

        // invoice validation
        if (getInvoiceNumber()!=null){
            if (invoiceNetAmountToUse()==null){
                b.append("no invoice net amount; ");
            }
            if (invoiceVatAmountToUse()==null && invoiceTaxToUse()!=null && !invoiceTaxToUse().equals("FRE")){
                b.append("no invoice vat amount; ");
            }
            if (invoiceGrossAmountToUse()==null){
                b.append("no invoice gross amount; ");
            }
            if (invoiceTaxToUse()==null || invoiceTaxToUse().equals("")){
                b.append("no invoice tax; ");
            }
        }

        //charge validation
        List<String> charges = Arrays.asList(
                "PROJECT MANAGEMENT",
                "TAX",
                "WORKS",
                "RELOCATION FEES",
                "ARCHITECT FEES",
                "LEGAL FEES",
                "MARKETING",
                "INSTALLATION WORKS",
                "SECURITY AGENTS",
                "OTHER"
        );
        if (getCharge()!=null && !charges.contains(getCharge())){
            b.append("charge unknown; ");
        }

        //tax validation
        List<String> taxcodes = Arrays.asList(
                "FRA",
                "FRC",
                "FRD",
                "FRE",
                "FRF",
                "FRO",
                "FRR",
                "FRS"
        );
        if (taxToUse()!=null && !taxcodes.contains(taxToUse())){
            b.append("tax unknown; ");
        }
        if (invoiceTaxToUse()!=null && !taxcodes.contains(invoiceTaxToUse())){
            b.append("invoice tax unknown; ");
        }

        //period validation
        if (getPeriod()!=null && !getPeriod().matches("F\\d{4}")){
            b.append("period unknown; ");
        }
        return b.length()==0 ? "OK" : b.toString();
    }

    @Override
    public void handleRow(final OrderInvoiceImportLine previousRow) {

            if(executionContext != null && excelFixture2 != null) {
                executionContext.addResult(excelFixture2,this.handle(previousRow));
            }

    }

}

