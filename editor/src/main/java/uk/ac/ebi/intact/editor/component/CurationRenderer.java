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


import uk.ac.ebi.intact.editor.util.ComponentUtils;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import java.io.IOException;

/**
 * TODO comment this class header.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@ResourceDependencies({
	@ResourceDependency(library="components", name="editor/curation.js", target = "head")
})
@FacesRenderer(componentFamily = "uk.ac.ebi.intact.editor.component.Curation", rendererType="uk.ac.ebi.intact.editor.component.CurationRenderer")
public class CurationRenderer extends Renderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered()) {
            return;
        }

        super.encodeEnd(context, component);

        UIComponent parent = component.getParent();

        String javascript = "var editorCuration = new EditorCuration(); editorCuration.setUnsavedChanges(true);";

        ComponentUtils.writeEventListenerScript(context, parent, new String[] {"keyup", "valueChange"}, javascript);

    }



}
