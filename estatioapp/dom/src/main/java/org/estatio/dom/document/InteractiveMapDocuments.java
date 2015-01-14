/*
 *  Copyright 2015 Eurocommercial Properties NV
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
package org.estatio.dom.document;

import java.util.List;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.value.Blob;

import org.estatio.app.interactivemap.InteractiveMapForFixedAssetRepresentation;
import org.estatio.app.interactivemap.InteractiveMapForFixedAssetService;
import org.estatio.dom.EstatioDomainService;
import org.estatio.dom.asset.FixedAsset;
import org.estatio.dom.asset.Property;
import org.isisaddons.wicket.svg.cpt.applib.InteractiveMap;

import javax.inject.Inject;

@DomainService()
@DomainServiceLayout(named = "Other", menuBar = DomainServiceLayout.MenuBar.PRIMARY, menuOrder = "80.10")
public class InteractiveMapDocuments extends EstatioDomainService<InteractiveMapDocument> {

    public InteractiveMapDocuments()
    {
        super(InteractiveMapDocuments.class, InteractiveMapDocument.class);
    }

    public String getId() {
        return "documents";
    }

    public String iconName() {
        return "Document";
    }

    @MemberOrder(sequence = "2")
    public List<InteractiveMapDocument> allDocuments() {
        return container.allInstances(InteractiveMapDocument.class);
    }

    @Programmatic
    public InteractiveMapDocument findByFixedAsset(FixedAsset fixedAsset) {
        return firstMatch("findByFixedAsset", "fixedAsset", fixedAsset);
    }

    @MemberOrder(sequence = "1")
    public InteractiveMapDocument newDocument(
            final @ParameterLayout(named = "Name") String name,
            final @ParameterLayout(named = "File") Blob file,
            final FixedAsset fixedAsset) {
        InteractiveMapDocument document = container.newTransientInstance(InteractiveMapDocument.class);
        document.setName(name);
        document.setFile(file);
        document.setFixedAsset(fixedAsset);
        container.persist(document);
        return document;
    }

    @NotInServiceMenu
    @ActionLayout(named = "Open")
    public InteractiveMap openDocument(
            final InteractiveMapDocument document,
            final @ParameterLayout(named = "Representation")InteractiveMapForFixedAssetRepresentation representation
    ) {
        FixedAsset fixedAsset = document.getFixedAsset();
        if (!(fixedAsset instanceof Property)) {
            return null;
        }

        InteractiveMap interactiveMap = interactiveMapService.showMap((Property) fixedAsset, representation);
        return interactiveMap;
    }

    // //////////////////////////////////////

    @Inject
    private InteractiveMapForFixedAssetService interactiveMapService;

    @javax.inject.Inject
    private DomainObjectContainer container;
}
