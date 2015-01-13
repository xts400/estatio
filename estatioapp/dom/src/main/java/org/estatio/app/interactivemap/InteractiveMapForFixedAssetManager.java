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
package org.estatio.app.interactivemap;

import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.ViewModel;

import org.isisaddons.wicket.svg.cpt.applib.InteractiveMap;

import org.estatio.app.EstatioViewModel;
import org.estatio.dom.asset.Property;

@Immutable
@Bookmarkable
@ViewModel
public class InteractiveMapForFixedAssetManager extends EstatioViewModel {

    public InteractiveMapForFixedAssetManager(final Property property, final InteractiveMapForFixedAssetRepresentation representation) {
        this.property = property;
        this.representationStr = representation.name();
    }

    // //////////////////////////////////////

    private Property property;

    @Title(sequence = "1")
    @MemberOrder(sequence = "1")
    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    // //////////////////////////////////////

    @Named("Select")
    @MemberOrder(name = "property", sequence = "1")
    public InteractiveMapForFixedAssetManager selectProperty(
            final Property property) {
        setProperty(property);
        return this;
    }

    // //////////////////////////////////////

    private String representationStr;

    @Hidden
    public String getRepresentationStr() {
        return representationStr;
    }

    public void setRepresentationStr(String representationStr) {
        this.representationStr = representationStr;
    }

    public InteractiveMapForFixedAssetRepresentation getRepresentation() {
        return InteractiveMapForFixedAssetRepresentation.valueOf(representationStr);
    }

    // //////////////////////////////////////

    @Named("Select")
    @MemberOrder(name = "representation", sequence = "1")
    public InteractiveMapForFixedAssetManager selectRepresentation(
            final @Named("Representation") InteractiveMapForFixedAssetRepresentation representation) {
        setRepresentationStr(representation.name());
        return this;
    }

    // //////////////////////////////////////

    public InteractiveMap getInteractiveMap() {
        return interactiveMapService.showMap(getProperty(), getRepresentation());
    }

    // //////////////////////////////////////

    @javax.inject.Inject
    private InteractiveMapForFixedAssetService interactiveMapService;

}
