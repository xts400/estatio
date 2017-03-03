/*
 *
 *  Copyright 2012-2015 Eurocommercial Properties NV
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
package org.estatio.dom.budgeting.partioning;

import java.util.List;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;

import org.estatio.dom.UdoDomainRepositoryAndFactory;
import org.estatio.dom.asset.Property;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;

@DomainService(repositoryFor = PartitionItem.class, nature = NatureOfService.DOMAIN)
@DomainServiceLayout()
public class PartitioningRepository extends UdoDomainRepositoryAndFactory<Partitioning> {

    public PartitioningRepository() {
        super(PartitioningRepository.class, Partitioning.class);
    }

    public Partitioning newPartitioning(
            final Property property,
            final LocalDate startDate,
            final LocalDate endDate,
            final BudgetCalculationType type) {
        Partitioning partitioning = newTransientInstance(Partitioning.class);
        partitioning.setProperty(property);
        partitioning.setStartDate(startDate);
        partitioning.setEndDate(endDate);
        partitioning.setType(type);
        persistIfNotAlready(partitioning);
        return partitioning;
    }

    public String validateNewPartitioning(
            final Property property,
            final LocalDate startDate,
            final LocalDate endDate,
            final BudgetCalculationType type){
        if (findUnique(property, type, startDate)!=null){
            return "This partitioning already exists";
        }
        // TODO !! Not valid anymore
        if (type == BudgetCalculationType.BUDGETED && findByPropertyAndType(property, BudgetCalculationType.BUDGETED).size() > 0){
            return "Only one partitioning of type BUDGETED is supported";
        }
        return null;
    }

    public Partitioning findUnique(final Property property, final BudgetCalculationType type, final LocalDate startDate){
        return uniqueMatch("findUnique", "property", property, "type", type, "startDate", startDate);
    }

    public List<Partitioning> findByPropertyAndType(final Property property, final BudgetCalculationType type){
        return allMatches("findByPropertyAndType", "property", property, "type", type);
    }

    public List<Partitioning> allPartitionings() {
        return allInstances();
    }


}
