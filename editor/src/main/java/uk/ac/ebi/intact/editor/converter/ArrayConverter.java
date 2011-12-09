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
package uk.ac.ebi.intact.editor.converter;

import org.apache.commons.lang.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@FacesConverter( value = "arrayConverter" )
public class ArrayConverter implements Converter {

    @Override
    public Object getAsObject( FacesContext facesContext, UIComponent uiComponent, String arrStr ) throws ConverterException {
        if ( arrStr == null || arrStr.trim().isEmpty()) return new String[0];

        arrStr = arrStr.replaceAll(" ", "");
        arrStr = arrStr.replaceAll("\r", "");

        String[] lines = arrStr.split("\n");
        List<String> items = new ArrayList<String>();

        for (String line : lines) {
            String[] tokens = line.split(",");
            items.addAll(Arrays.asList(tokens));
        }

        return items.toArray(new String[items.size()]);
    }

    @Override
    public String getAsString( FacesContext facesContext, UIComponent uiComponent, Object o ) throws ConverterException {
        if ( o == null ) return "";

        if ( o instanceof String[] ) {
            String[] arr = ( String[] ) o;
            return StringUtils.join(arr, System.getProperty("line.separator"));
        } else {
            throw new IllegalArgumentException( "Argument must be an array of Strings: " + o + " (" + o.getClass() + ")" );
        }
    }
}