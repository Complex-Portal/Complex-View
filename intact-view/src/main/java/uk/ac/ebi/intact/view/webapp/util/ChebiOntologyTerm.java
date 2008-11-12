/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.util;

import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.bridges.ontologies.term.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;

import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public class ChebiOntologyTerm implements OntologyTerm {

    public static final String CHEBI_NAME = "ChEBI Ontology";

    private OntologyIndexSearcher ontologyIndexSearcher;


    public ChebiOntologyTerm( OntologyIndexSearcher ontologyIndexSearcher ) {
        this.ontologyIndexSearcher = ontologyIndexSearcher;
    }

    public String getId() {
        return "";
    }

    public String getName() {
        return CHEBI_NAME;
    }


    public List<OntologyTerm> getParents() {
        return Collections.EMPTY_LIST;
    }

    public List<OntologyTerm> getChildren() {
        List<OntologyTerm> children = new ArrayList<OntologyTerm>( 3 );

        children.add( new LazyLoadedOntologyTerm( ontologyIndexSearcher, "CHEBI:36342", "Subatomic particle" ) );
        children.add( new LazyLoadedOntologyTerm( ontologyIndexSearcher, "CHEBI:24431", "Molecular structure" ) );
        children.add( new LazyLoadedOntologyTerm( ontologyIndexSearcher, "CHEBI:50906", "role" ) );

        return children;
    }

    public List<OntologyTerm> getParents( boolean includeCyclic ) {
        return getParents();
    }

    public List<OntologyTerm> getChildren( boolean includeCyclic ) {
        return getChildren();
    }

    public Set<OntologyTerm> getAllParentsToRoot() {
        return Collections.EMPTY_SET;
    }

    public Collection<OntologyTerm> getChildrenAtDepth( int depth ) {
        throw new UnsupportedOperationException( "Root does not support this operation" );
    }


}
