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

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

/**
 * TODO comment this class header.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@FacesComponent("uk.ac.ebi.intact.editor.component.DelayedEvent")
public class DelayedEvent extends UIComponentBase {

     public static final String COMPONENT_FAMILY = "uk.ac.ebi.intact.editor.component.Delayed";
     public static final String RENDERER_TYPE = "uk.ac.ebi.intact.editor.component.DelayedEventRenderer";

    private String forId;
    private String timeout = "1000";

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public String getRendererType() {
        return RENDERER_TYPE;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getForId() {
        return forId;
    }

    public void setForId(String forId) {
        this.forId = forId;
    }
}
