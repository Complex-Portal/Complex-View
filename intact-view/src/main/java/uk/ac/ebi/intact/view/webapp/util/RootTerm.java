/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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

import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

import java.util.*;

/**
 * Represents a root term in an ontology.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RootTerm implements OntologyTerm {

    private OntologySearcher ontologySearcher;
    private String name;
    private List<OntologyTerm> children;

    public RootTerm(OntologySearcher ontologySearcher, String name) {
        this(ontologySearcher, name, new LinkedList<OntologyTerm>());
    }

    public RootTerm(OntologySearcher ontologySearcher, String name, List<OntologyTerm> children) {
        this.ontologySearcher = ontologySearcher;
        this.name = name;
        this.children = children;
    }

    public boolean addChild(String id, String name) {
        try {
            return children.add(new LazyLoadedOntologyTerm( ontologySearcher, id, name ));
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    public String getId() {
        return "";
    }

    public String getName() {
        return name;
    }


    public List<OntologyTerm> getParents() {
        return Collections.EMPTY_LIST;
    }

    public List<OntologyTerm> getChildren() {
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

    public OntologySearcher getOntologySearcher() {
        return ontologySearcher;
    }
}
