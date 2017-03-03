/*
 * Copyright 2012-2015 Eurocommercial Properties NV
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.estatio.dom.budgeting.keytable;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;

import org.estatio.dom.UdoDomainRepositoryAndFactory;
import org.estatio.dom.budgeting.partioning.Partitioning;

@DomainService(repositoryFor = KeyTable.class, nature = NatureOfService.DOMAIN)
@DomainServiceLayout()
public class KeyTableRepository extends UdoDomainRepositoryAndFactory<KeyTable> {

    public KeyTableRepository() {
        super(KeyTableRepository.class, KeyTable.class);
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public KeyTable newKeyTable(
            final Partitioning partitioning,
            final String name,
            final FoundationValueType foundationValueType,
            final KeyValueMethod keyValueMethod,
            final Integer numberOfDigits) {
        KeyTable keyTable = newTransientInstance();
        keyTable.setPartitioning(partitioning);
        keyTable.setName(name);
        keyTable.setFoundationValueType(foundationValueType);
        keyTable.setKeyValueMethod(keyValueMethod);
        keyTable.setPrecision(numberOfDigits);
        persistIfNotAlready(keyTable);

        return keyTable;
    }

    public String validateNewKeyTable(
            final Partitioning partitioning,
            final String name,
            final FoundationValueType foundationValueType,
            final KeyValueMethod keyValueMethod,
            final Integer numberOfDigits) {
        if (findByPartitioningAndName(partitioning, name)!=null) {
            return "There is already a keytable with this name for this partitioning";
        }

        return null;
    }

    @Programmatic
    public KeyTable findOrCreateBudgetKeyTable(
            final Partitioning partitioning,
            final String name,
            final FoundationValueType foundationValueType,
            final KeyValueMethod keyValueMethod,
            final Integer precision
    ) {
        final KeyTable keyTable = findByPartitioningAndName(partitioning, name);
        if (keyTable !=null) {
            return keyTable;
        } else {
            return newKeyTable(partitioning, name, foundationValueType, keyValueMethod, precision);
        }
    }


    @Programmatic
    public List<KeyTable> allKeyTables() {
        return allInstances();
    }


    @Programmatic
    public KeyTable findByPartitioningAndName(final Partitioning partitioning, final String name) {
        return uniqueMatch("findByPartitioningAndName", "partitioning", partitioning, "name", name);
    }


    public List<KeyTable> findByPartioning(Partitioning partitioning) {
        return allMatches("findByPartioning", "partitioning", partitioning);
    }


    @ActionLayout(hidden = Where.EVERYWHERE)
    public List<KeyTable> autoComplete(final String search) {
        return allMatches("findKeyTableByNameMatches", "name", search.toLowerCase());
    }

}
