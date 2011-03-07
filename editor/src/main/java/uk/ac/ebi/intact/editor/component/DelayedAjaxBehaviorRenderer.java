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
import org.primefaces.util.ComponentUtils;

import javax.faces.FacesException;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
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
//        return super.getScript(behaviorContext, behavior);
        String timeout = "500";
        String beforeTimeout = "";

        if (behavior instanceof DelayedAjaxBehavior) {
            DelayedAjaxBehavior dab = (DelayedAjaxBehavior) behavior;
            timeout = dab.getTimeout();

            if (dab.getBeforeTimeoutEvent() != null && !dab.getBeforeTimeoutEvent().isEmpty()) {
                beforeTimeout = dab.getBeforeTimeoutEvent()+";";
            }
        }

        String function = "function f"+System.currentTimeMillis()+"() { "+ getHackedScript(behaviorContext, behavior)+" }";

        return beforeTimeout+" ia_delay("+function+","+timeout+"); ";
    }

    /**
     * HACK: this method corresponds to the getScript(...) method in the super class, just changing one line (indicated below)
     * @param behaviorContext
     * @param behavior
     * @return
     */
    public String getHackedScript(ClientBehaviorContext behaviorContext, ClientBehavior behavior) {
        AjaxBehavior ajaxBehavior = (AjaxBehavior) behavior;
        FacesContext fc = behaviorContext.getFacesContext();
        UIComponent component = behaviorContext.getComponent();
        String clientId = component.getClientId(fc);
        String url = fc.getApplication().getViewHandler().getActionURL(fc, fc.getViewRoot().getViewId());
		url =  fc.getExternalContext().encodeResourceURL(url);

        UIComponent form = ComponentUtils.findParentForm(fc, component);
		if(form == null) {
			throw new FacesException("AjaxBehavior for : \"" + component.getClientId(fc) + "\" must be inside a form element");
		}

        StringBuilder req = new StringBuilder();
        req.append("PrimeFaces.ajax.AjaxRequest(");

        //url
        req.append("'").append(url).append("'");

        //options
        req.append(",{formId:'").append(form.getClientId(fc)).append("'");
        req.append(",async:").append(ajaxBehavior.isAsync());
        req.append(",global:").append(ajaxBehavior.isGlobal());

        //source

        // HACK, the following line has been updated to work with the delayed invocation

        req.append(",source:'").append(clientId).append("'");

        //process
        String process = ajaxBehavior.getProcess() != null ? ComponentUtils.findClientIds(fc, component, ajaxBehavior.getProcess()) : clientId;
        req.append(",process:'").append(process).append("'");

        //update
        if (ajaxBehavior.getUpdate() != null) {
            req.append(",update:'").append(ComponentUtils.findClientIds(fc, component, ajaxBehavior.getUpdate())).append("'");
        }

        //behavior event
        req.append(",event:'").append(behaviorContext.getEventName()).append("'");

        //callbacks
        if (ajaxBehavior.getOnstart() != null)
            req.append(",onstart:function(xhr){").append(ajaxBehavior.getOnstart()).append(";}");
        if (ajaxBehavior.getOnerror() != null)
            req.append(",onerror:function(xhr, status, error){").append(ajaxBehavior.getOnerror()).append(";}");
        if (ajaxBehavior.getOnsuccess() != null)
            req.append(",onsuccess:function(data, status, xhr, args){").append(ajaxBehavior.getOnsuccess()).append(";}");
        if (ajaxBehavior.getOncomplete() != null)
            req.append(",oncomplete:function(xhr, status, args){").append(ajaxBehavior.getOncomplete()).append(";}");

        req.append("}");

        //params
        boolean firstParam = true, hasParam = false;

        for (UIComponent child : component.getChildren()) {
            if (child instanceof UIParameter) {
                UIParameter parameter = (UIParameter) child;
                hasParam = true;

                if (firstParam) {
                    firstParam = false;
                    req.append(",{");
                } else {
                    req.append(",");
                }

                req.append("'").append(parameter.getName()).append("':'").append(parameter.getValue()).append("'");
            }

            if (hasParam)
                req.append("}");
        }

        req.append(");");

        return req.toString();
    }


}
