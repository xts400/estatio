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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.applib.services.linking.DeepLinkService;
import org.apache.isis.applib.value.Blob;

import org.isisaddons.wicket.svg.cpt.applib.InteractiveMap;
import org.isisaddons.wicket.svg.cpt.applib.InteractiveMapAttribute;
import org.isisaddons.wicket.svg.cpt.applib.InteractiveMapElement;

import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.Unit;
import org.estatio.dom.asset.Units;
import org.estatio.dom.document.InteractiveMapDocument;
import org.estatio.dom.document.InteractiveMapDocuments;

@DomainService
public class InteractiveMapForFixedAssetService extends AbstractService {

    @Prototype
    // TODO: Work in progress, therefore only accessible in prototype mode
    @ActionSemantics(Of.SAFE)
    @NotInServiceMenu
    public InteractiveMapForFixedAssetManager maps(Property property) {
        return new InteractiveMapForFixedAssetManager(property, InteractiveMapForFixedAssetRepresentation.DEFAULT);
    }

    @ActionSemantics(Of.SAFE)
    @NotInServiceMenu
    public InteractiveMap showMap(
            final Property property,
            final @ParameterLayout(named = "representation") InteractiveMapForFixedAssetRepresentation representation) {
        if (property == null || representation == null) {
            return null;
        }

        Map<Color, Integer> colorMap = new HashMap<>();

        InteractiveMapDocument document = documents.findByFixedAsset(property);

        InteractiveMapForFixedAssetColorService colorService = representation.getColorService();
        getContainer().injectServicesInto(colorService);

        try {
            String svgString = new String(document.getFile().getBytes(), "UTF-8");
            InteractiveMap interactiveMap = new InteractiveMap(svgString);
            interactiveMap.setTitle(document.getName());
            for (Unit unit : units.findByProperty(property)) {
                final Color color = colorService.getColor(unit);
                colorMap = ColorMapHelper.addToMap(colorMap, color);

                // shape
                InteractiveMapElement element = new InteractiveMapElement(unit.getReference());
                if (color != null) {
                    element.addAttribute(new InteractiveMapAttribute("fill", color.getColor()));
                }
                URI link = deepLinkService.deepLinkFor(unit);
                element.addAttribute(new InteractiveMapAttribute("xlink:href", link.toString()));
                interactiveMap.addElement(element);

                // label
                // interactiveMap.addElement(new InteractiveMapElement("label" +
                // i.toString(), unit.getDescription()));

                // sub label
                // interactiveMap.addElement(new
                // InteractiveMapElement("subLabel" + i.toString(),
                // unit.getCategory().name()));

            }

            int legendId = 1;
            for (Color color : ColorMapHelper.sortByValue(colorMap)) {
                // // label
                // interactiveMap.addElement(new
                // InteractiveMapElement(String.format("legend%dText",
                // legendId), color.getLabel()));
                // // color
                // final InteractiveMapElement element = new
                // InteractiveMapElement(String.format("legend%dShape",
                // legendId));
                // element.addAttribute(new InteractiveMapAttribute("fill",
                // color.getColor()));
                // interactiveMap.addElement(element);
                // legendId++;
            }

            return interactiveMap;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public boolean hideShowMap(final Property property, final InteractiveMapForFixedAssetRepresentation representation) {
        return shouldShowSvgActions(property);
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @NotInServiceMenu
    public Blob downloadMap(final Property property, final InteractiveMapForFixedAssetRepresentation representation) {
        InteractiveMap interactiveMap = showMap(property, representation);
        String svgContent = interactiveMap.parse();
        StringReader input = new StringReader(svgContent);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Transcoder transcoder = new PDFTranscoder();
        try {
            transcoder.transcode(new TranscoderInput(input), new TranscoderOutput(output));
        } catch (TranscoderException tx) {
            throw new RuntimeException("An error occurred while transcoding SVG document '"
                    + interactiveMap.getTitle() + "' to PDF", tx);
        }

        return new Blob(property.getName() + ".pdf", "application/pdf", output.toByteArray());
    }

    public boolean hideDownloadMap(final Property property, final InteractiveMapForFixedAssetRepresentation representation) {
        return shouldShowSvgActions(property);
    }

    // //////////////////////////////////////

    private boolean shouldShowSvgActions(final Property property) {
        InteractiveMapDocument document = documents.findByFixedAsset(property);
        return document == null;
    }

    @Inject
    private InteractiveMapDocuments documents;

    @Inject
    private Units units;

    @Inject
    private DeepLinkService deepLinkService;
}
