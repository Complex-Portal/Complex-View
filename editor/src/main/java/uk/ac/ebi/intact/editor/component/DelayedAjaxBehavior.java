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

import javax.faces.component.behavior.FacesBehavior;

/**
 * TODO comment this class header.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@FacesBehavior("uk.ac.ebi.intact.editor.component.DelayedAjaxBehavior")
public class DelayedAjaxBehavior extends AjaxBehavior {

    private String timeout = "1000";
    private String beforeTimeoutEvent;

    @Override
    public String getRendererType() {
        return "uk.ac.ebi.intact.editor.component.DelayedAjaxBehaviorRenderer";
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getBeforeTimeoutEvent() {
        return beforeTimeoutEvent;
    }

    public void setBeforeTimeoutEvent(String beforeTimeoutEvent) {
        this.beforeTimeoutEvent = beforeTimeoutEvent;
    }
}
