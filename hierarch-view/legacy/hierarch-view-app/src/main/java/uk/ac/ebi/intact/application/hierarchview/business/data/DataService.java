/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.business.data;

import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.IntactObject;

import javax.persistence.Query;
import java.util.Collection;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id
 * @since 1.6.0-SNAPSHOT
 */
public interface DataService  <C extends CvTopic, I extends Interactor, T extends IntactObject> {

    C getCvObjectByShortLabel( String shortlabel );

    I getInteractorByAc( String ac );

    I getProteinByAc( String ac );

    Collection getProteinByAcLike( String ac );

    Collection<T> getColByPropertyName( Class<T> objectType, String searchParam, String searchValue );

    String getDbName() throws SQLException;

    Query createQuery( String query);

    Connection connection();
    
}
