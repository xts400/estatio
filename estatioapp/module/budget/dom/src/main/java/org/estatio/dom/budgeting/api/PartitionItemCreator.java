package org.estatio.dom.budgeting.api;

import java.math.BigDecimal;

import org.estatio.dom.budgeting.partioning.PartitionItem;
import org.estatio.dom.budgeting.keytable.KeyTable;
import org.estatio.dom.charge.Charge;

public interface PartitionItemCreator {

    PartitionItem updateOrCreatePartitionItem(
            final Charge invoiceCharge,
            final KeyTable keyTable,
            final BigDecimal percentage
    );

}
