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
package uk.ac.ebi.intact.editor.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import java.io.IOException;

/**
 * TODO comment this class header.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
//@ResourceDependencies({
//	@ResourceDependency(library="components", name="editor/delayedEvent.js")
//})
@FacesRenderer(componentFamily = "uk.ac.ebi.intact.editor.component.Delayed", rendererType="uk.ac.ebi.intact.editor.component.DelayedEventRenderer")
public class DelayedEventRenderer extends Renderer {

    private static final Log log = LogFactory.getLog(DelayedEventRenderer.class);

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        ResponseWriter writer = context.getResponseWriter();
		String clientId = component.getClientId(context);

        DelayedEvent delayedEvent = (DelayedEvent) component;

        UIComponent delayedComp = component.findComponent(delayedEvent.getForId());

        if (delayedComp != null) {

            if (delayedComp instanceof HtmlCommandButton) {
                HtmlCommandButton commandButton = (HtmlCommandButton) delayedComp;

                String timeoutFunction = "delayEvent_"+commandButton.getClientId()+"()";

                writeJavascript(timeoutFunction, writer, delayedEvent, commandButton.getClientId(), commandButton.getOnclick());

                commandButton.setOnclick(timeoutFunction);

            } else {
                log.error("Cannot delay component: " + delayedEvent.getForId());
            }

        } else {
            log.error("Component with could not be found by delayedEvent: " + delayedEvent.getForId());
        }

        super.encodeEnd(context, component);
    }

    private void writeJavascript(String timeoutFunction, ResponseWriter writer, DelayedEvent delayedEvent, String clientId, String delayedJavascript) throws IOException {
        String delayedFunction = "delayedScript_"+clientId+"()";
        String divId = "delayedDiv_"+clientId;

        writer.startElement("span", delayedEvent);
        writer.writeAttribute("id", divId, null);
        writer.endElement("span");

        writer.startElement("script", delayedEvent);
        writer.writeAttribute("type", "text/javascript", null);

        writer.write("\nfunction "+timeoutFunction+" {\n");

        writer.write("document.getElementById('"+divId+"').focus();\n");
        writer.write("setTimeout('"+delayedFunction+"', "+delayedEvent.getTimeout()+");");

        writer.write("}\n");

        writer.write("\nfunction "+delayedFunction+" {\n");
        writer.write(delayedJavascript+";");
        //writer.write("alert('lololo');");
        writer.write("\n}\n");

        writer.endElement("script");

    }
}
