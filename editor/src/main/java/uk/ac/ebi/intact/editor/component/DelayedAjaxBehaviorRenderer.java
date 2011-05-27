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


import org.primefaces.component.behavior.ajax.AjaxBehavior;
import org.primefaces.component.behavior.ajax.AjaxBehaviorRenderer;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesBehaviorRenderer;

/**
 * TODO comment this class header.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@ResourceDependencies({
	@ResourceDependency(library="components", name="editor/delayedAjax.js")
})
@FacesBehaviorRenderer(rendererType="uk.ac.ebi.intact.editor.component.DelayedAjaxBehaviorRenderer")
public class DelayedAjaxBehaviorRenderer extends AjaxBehaviorRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component, ClientBehavior behavior) {
        super.decode(context, component, behavior);

    }

    @Override
    public String getScript(ClientBehaviorContext behaviorContext, ClientBehavior behavior) {
        AjaxBehavior ajaxBehavior = (AjaxBehavior) behavior;
        if(ajaxBehavior.isDisabled()) {
            return null;
        }

        String timeout = "500";
        String beforeTimeout = "";

        if (behavior instanceof DelayedAjaxBehavior) {
            DelayedAjaxBehavior dab = (DelayedAjaxBehavior) behavior;
            timeout = dab.getTimeout();

            if (dab.getBeforeTimeoutEvent() != null && !dab.getBeforeTimeoutEvent().isEmpty()) {
                beforeTimeout = dab.getBeforeTimeoutEvent()+";";
            }
        }

        String function = "function f"+System.currentTimeMillis()+"() { "+ super.getScript(behaviorContext, behavior)+" }";

        return beforeTimeout+" ia_delay("+function+","+timeout+"); ";
    }


}
