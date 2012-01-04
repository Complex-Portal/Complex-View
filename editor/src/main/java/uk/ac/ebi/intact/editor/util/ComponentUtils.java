/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

/**
 * Utility methods for the components.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public final class ComponentUtils {

    private ComponentUtils() {}

    public static void writeEventListenerScript(FacesContext context, UIComponent component, String event, String javascript) throws IOException {
        writeEventListenerScript(context, component, new String[] {event}, javascript);
    }

    public static void writeEventListenerScript(FacesContext context, UIComponent component, String[] events, String javascript) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);

        String callbackFunction = "curationChanged_"+System.currentTimeMillis();

        writer.write("function "+callbackFunction+"(e) { ");
        writer.write(javascript+";");
        writer.write(" }\n");

        for (String event : events) {
            String jQueryFriendlyId = component.getClientId().replaceAll(":", "\\\\\\\\:");
            writer.write("$('#"+jQueryFriendlyId+"').bind('"+event+"', "+callbackFunction+"); ");
        }

        writer.endElement("script");
    }
}
