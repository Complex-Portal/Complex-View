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
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@FacesConverter( value = "multipleUrlConverter", forClass = URL[].class )
public class MultipleUrlConverter implements Converter {

    @Override
    public Object getAsObject( FacesContext facesContext, UIComponent uiComponent, String urlsStr ) throws ConverterException {
        if ( urlsStr == null || urlsStr.trim().isEmpty()) return new URL[0];

        String[] lines = urlsStr.split(System.getProperty("line.separator"));
        URL[] urls = new URL[lines.length];

        for (int i=0; i<lines.length; i++) {
            try {
                urls[i] = new URL(lines[i]);
            } catch (MalformedURLException e) {
                throw new ConverterException("Bad URL found: "+lines[i]);
            }
        }

        return urls;
    }

    @Override
    public String getAsString( FacesContext facesContext, UIComponent uiComponent, Object o ) throws ConverterException {
        if ( o == null ) return "";

        if ( o instanceof URL[] ) {
            URL[] urls = ( URL[] ) o;
            return StringUtils.join(urls, System.getProperty("line.separator"));
        } else {
            throw new IllegalArgumentException( "Argument must be an array of URLs: " + o + " (" + o.getClass() + ")" );
        }
    }
}