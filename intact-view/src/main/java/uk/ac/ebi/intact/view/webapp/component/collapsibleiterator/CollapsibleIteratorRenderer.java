/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.component.collapsibleiterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@FacesRenderer(rendererType = CollapsibleIteratorRenderer.RENDERER_TYPE, componentFamily = CollapsibleIterator.COMPONENT_FAMILY)
public class CollapsibleIteratorRenderer extends Renderer {

    private static final Log log = LogFactory.getLog( CollapsibleIteratorRenderer.class );

    public static final String RENDERER_TYPE = "uk.ac.ebi.intact.view.CollapsibleIteratorRenderer";

    public CollapsibleIteratorRenderer() {
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        super.encodeBegin(context, component);
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        CollapsibleIterator collapsibleIterator = (CollapsibleIterator) component;

                ResponseWriter writer = context.getResponseWriter();

        final String divId = component.getClientId() + "_additional";

        boolean containsAdditional = false;
        int n = 0;

        final List items = collapsibleIterator.getValuesAsList();

        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
            Object item = iterator.next();

            context.getELContext().getELResolver().setValue(context.getELContext(), null, "item", item);

            if (n == collapsibleIterator.getMaxShown()) {
                containsAdditional = true;

                writer.startElement("div", component);
                writer.writeAttribute("id", divId, null);
                writer.writeAttribute("class", "additional", null);

                if (collapsibleIterator.isDisclosed()) {
                    writer.writeAttribute("style", "display: inline", null);
                } else {
                    writer.writeAttribute("style", "display: none", null);
                }
            }

            final List<UIComponent> children = component.getChildren();

            for (UIComponent child : children) {
                child.encodeAll(context);
            }

            if (iterator.hasNext()) {
                writer.write("<br/>");
            }

            context.getELContext().getELResolver().setValue(context.getELContext(), null, "item", null);

            n++;
        }

        if (containsAdditional) {
            writer.endElement("div");
        }


        if (items.size() - collapsibleIterator.getMaxShown() > 0) {
            writer.startElement("a", component);

            String spanId1 = divId+"_s1";
            String spanId2 = divId+"_s2";

            writer.startElement("span", component);

            writer.writeAttribute("id", spanId1, null);
            writer.writeAttribute("style", collapsibleIterator.isDisclosed()? "display: inline" : "display:none", null);
            writer.writeAttribute("onclick", "document.getElementById('" + divId + "').style.display='none'; document.getElementById('"+spanId1+"').style.display='none'; document.getElementById('"+spanId2+"').style.display='inline';", null);
            writer.write("<br/>");
            writer.startElement("strong", component);
            writer.write("[-]");
            writer.endElement("strong");
            writer.endElement("span");

            writer.startElement("span", component);
            writer.writeAttribute("id", spanId2, null);
            writer.writeAttribute("style", collapsibleIterator.isDisclosed()? "display: none" : "display:inline", null);
            writer.writeAttribute("onclick", "document.getElementById('" + divId + "').style.display='inline'; document.getElementById('"+spanId1+"').style.display='inline'; document.getElementById('"+spanId2+"').style.display='none';", null);
            writer.write("<br/>");
            writer.startElement("strong", component);
            writer.write("[+" + (items.size() - collapsibleIterator.getMaxShown()) + "]");
            writer.endElement("strong");

            writer.endElement("span");

            writer.endElement("a");
        }

    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        super.encodeEnd(context, component);


    }

}
