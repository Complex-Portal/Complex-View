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
import uk.ac.ebi.intact.jami.dao.OrganismDao;
import uk.ac.ebi.intact.jami.model.extension.IntactOrganism;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
@FacesConverter( value = "organismConverter", forClass = IntactOrganism.class )
public class OrganismConverter implements Converter {

    @Override
    public Object getAsObject( FacesContext facesContext, UIComponent uiComponent, String ac ) throws ConverterException {
        if ( ac == null ) return null;

        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        OrganismDao orgDao = dao.getOrganismDao();
        return orgDao.getByAc(ac);
    }

    @Override
    public String getAsString( FacesContext facesContext, UIComponent uiComponent, Object o ) throws ConverterException {
        if ( o == null ) return null;

        if ( o instanceof IntactOrganism ) {
            IntactOrganism bioSource = ( IntactOrganism ) o;
            return bioSource.getAc();
        } else {
            throw new IllegalArgumentException( "Argument must be a BioSource: " + o + " (" + o.getClass() + ")" );
        }
    }
}