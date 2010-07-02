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
package uk.ac.ebi.intact.editor.controller.curate.cvobject;

import uk.ac.ebi.intact.model.CvObject;

import java.util.Collections;
import java.util.List;

/**
* @author Bruno Aranda (baranda@ebi.ac.uk)
* @version $Id$
*/
public class CvObjectAutocompleter {

    private String cvClassName;

    public CvObjectAutocompleter(String cvClassName) {
        this.cvClassName = cvClassName;
    }

    public List<CvObject> autocomplete( String queryStr ) {

    //Query query = inputCvObjectController.type.createQuery( "select cv from CvObject cv where cv.shortLabel like :query order by cv.shortLabel asc" );
    //query.setParameter( "query", queryStr + "%" );

    //return query.getResultList();
        return Collections.EMPTY_LIST;
}
}
