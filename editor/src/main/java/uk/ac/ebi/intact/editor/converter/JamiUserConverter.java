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

import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@FacesConverter( value = "jamiUserConverter", forClass = uk.ac.ebi.intact.jami.model.user.User.class )
public class JamiUserConverter implements Converter {

    @Override
    public Object getAsObject( FacesContext facesContext, UIComponent uiComponent, String loginName ) throws ConverterException {
        if ( loginName == null ) return null;

        uk.ac.ebi.intact.jami.dao.UserDao userDao = ((IntactDao)ApplicationContextProvider.getBean("intactDao")).getUserDao();
        return userDao.getByLogin(loginName);
    }

    @Override
    public String getAsString( FacesContext facesContext, UIComponent uiComponent, Object o ) throws ConverterException {
        if ( o == null ) return null;

        if ( o instanceof uk.ac.ebi.intact.jami.model.user.User) {
            uk.ac.ebi.intact.jami.model.user.User user = (uk.ac.ebi.intact.jami.model.user.User) o;
            return user.getLogin();
        } else {
            throw new IllegalArgumentException( "Argument must be a User: " + o + " (" + o.getClass() + ")" );
        }
    }
}