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

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@FacesComponent(CollapsibleIterator.COMPONENT_TYPE)
public class CollapsibleIterator extends UIComponentBase implements Serializable {

    private static final Log log = LogFactory.getLog( CollapsibleIterator.class );

    public static final String COMPONENT_TYPE = "uk.ac.ebi.intact.view.CollapsibleIterator";
    public static final String COMPONENT_FAMILY = "uk.ac.ebi.intact.view.CollapsibleIterator";
    public static final String DEFAULT_RENDERER = CollapsibleIteratorRenderer.RENDERER_TYPE;

    public CollapsibleIterator() {
        super();
    }

    @Override
    public String getFamily() {
      return CollapsibleIterator.COMPONENT_FAMILY;
   }

    @Override
    public String getRendererType() {
        return DEFAULT_RENDERER;
    }

    public Collection getItems() {
        List elements = getValuesAsList();

        if (isDisclosed()) {
            return elements;
        }

        return elements.subList(0, Math.min(getMaxShown(), elements.size()));
    }

    public List getValuesAsList() {
        final Object value = getValue();

        List elements;

        if (value instanceof Collection) {
             elements = new ArrayList((Collection) value);
         } else if (value instanceof Object[]) {
             Object[] s = (Object[]) value;
             elements = Arrays.asList(s);
         } else {
             throw new IllegalArgumentException("Value should be a collection or array: "+value);
         }
        return elements;
    }

    public boolean isShowControls() {
        return (getValuesAsList().size() > getMaxShown());
    }

    public Object getValue() {
        return getStateHelper().eval("value");
    }

    public void setValue(Object value) {
        getStateHelper().put("value", value);
    }

    public int getMaxShown() {
        return (Integer) getStateHelper().eval("maxShown");
    }

    public void setMaxShown(int maxShown) {
        getStateHelper().put("maxShown", maxShown);
    }

    public boolean isDisclosed() {
        return (Boolean) getStateHelper().eval("disclosed");
    }

    public void setDisclosed(boolean disclosed) {
        getStateHelper().put("disclosed", disclosed);
    }

}
