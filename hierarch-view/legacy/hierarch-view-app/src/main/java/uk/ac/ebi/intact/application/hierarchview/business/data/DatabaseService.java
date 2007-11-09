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

import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUser;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Interactor;

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
public class DatabaseService implements DataService{

    public DaoFactory getDaoFactory () {
        return IntactUser.getCurrentInstance().getDaoFactory();
    }

    public CvTopic getCvObjectByShortLabel( String shortlabel ) {
        return getDaoFactory().getCvObjectDao(CvTopic.class).getByShortLabel( shortlabel );
    }

    public Interactor getInteractorByAc( String ac ) {
        return getDaoFactory().getInteractorDao().getByAc( ac );
    }

    public Interactor getProteinByAc( String ac ) {
        return getDaoFactory().getProteinDao().getByAc( ac );
    }

    public Collection getProteinByAcLike( String ac ) {
        return getDaoFactory().getProteinDao().getByAcLike( ac );
    }

    public Collection getColByPropertyName( Class objectType, String searchParam, String searchValue ) {
        return getDaoFactory().getIntactObjectDao(objectType).getColByPropertyName(searchParam, searchValue);
    }

    public String getDbName() throws SQLException {
        return getDaoFactory().getBaseDao().getDbName();
    }

    public Query createQuery( String query ) {
        return getDaoFactory().getEntityManager().createQuery( query );
    }

    public Connection connection() {
        return getDaoFactory().connection();
    }    
}
