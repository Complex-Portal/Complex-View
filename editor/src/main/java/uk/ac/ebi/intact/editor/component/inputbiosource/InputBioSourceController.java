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
package uk.ac.ebi.intact.editor.component.inputbiosource;

import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.BioSource;

import javax.faces.event.ActionEvent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
public class InputBioSourceController {

    @PersistenceContext( unitName = "intact-core-default" )
    private EntityManager entityManager;

    private LazyDataModel<BioSource> bioSources;
    private List<BioSource> bioSources2;

    private BioSource selectedBioSource;

    public InputBioSourceController() {
    }

    public List<BioSource> autocomplete( String queryStr ) {
        Query query = entityManager.createQuery( "select b from BioSource b where b.shortLabel like :query order by b.shortLabel asc" );
        query.setParameter( "query", queryStr + "%" );
        return query.getResultList();
    }

    public void loadBioSources( ActionEvent evt ) {
        bioSources = LazyDataModelFactory.createLazyDataModel( entityManager, "select b from BioSource b order by b.shortLabel",
                                                               "select count(b) from BioSource b" );

        bioSources2 = new ArrayList<BioSource>();
        for ( int i = 0; i < 100; i++ ) {
            bioSources2.add( new IntactMockBuilder( IntactContext.getCurrentInstance().getInstitution() ).createBioSourceRandom() );
        }
    }

    public void selectBioSource( BioSource bioSource ) {
        setSelectedBioSource( bioSource );
    }

    public LazyDataModel<BioSource> getBioSources() {
        return bioSources;
    }

    public void setBioSources( LazyDataModel<BioSource> bioSources ) {
        this.bioSources = bioSources;
    }

    public BioSource getSelectedBioSource() {
        return selectedBioSource;
    }

    public void setSelectedBioSource( BioSource selectedBioSource ) {
        this.selectedBioSource = selectedBioSource;
    }


    public List<BioSource> getBioSources2() {
        return bioSources2;
    }

    public void setBioSources2( List<BioSource> bioSources2 ) {
        this.bioSources2 = bioSources2;
    }
}
