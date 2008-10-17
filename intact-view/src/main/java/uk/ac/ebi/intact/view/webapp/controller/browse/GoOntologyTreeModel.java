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
package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.apache.lucene.search.IndexSearcher;
import org.apache.myfaces.trinidad.model.ChildPropertyTreeModel;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;
import uk.ac.ebi.intact.bridges.ontologies.term.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;

import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class GoOntologyTreeModel extends ChildPropertyTreeModel {

    private String[] interactorColours = new String[] {"#73b360", "#84bc73", "#96c688", "#a7cf9b","#cae2c3", "#dcecd7"};

    public GoOntologyTreeModel(OntologyTermWrapper instance) {
        super(instance, "children");
    }

    public GoOntologyTreeModel(final OntologyIndexSearcher ontologyIndexSearcher,
                               final IndexSearcher interactorIndexSearcher,
                               final String baseQuery) {
        setChildProperty("children");

        OntologyTerm root = new OntologyTerm() {
            public String getId() {
                return "";
            }

            public String getName() {
                return "GO Ontology";
            }

            public List<OntologyTerm> getParents() {
                return Collections.EMPTY_LIST;
            }

            public List<OntologyTerm> getChildren() {
                List<OntologyTerm> children = new ArrayList<OntologyTerm>();

                children.add(new LazyLoadedOntologyTerm(ontologyIndexSearcher, "GO:0008150", "biological_process"));
                children.add(new LazyLoadedOntologyTerm(ontologyIndexSearcher, "GO:0003674", "molecular_function"));
                children.add(new LazyLoadedOntologyTerm(ontologyIndexSearcher, "GO:0005575", "cellular_component"));

                return children;
            }

            public List<OntologyTerm> getParents(boolean includeCyclic) {
                return getParents();
            }

            public List<OntologyTerm> getChildren(boolean includeCyclic) {
                return getChildren();
            }

            public Set<OntologyTerm> getAllParentsToRoot() {
                return Collections.EMPTY_SET;
            }

            public Collection<OntologyTerm> getChildrenAtDepth(int depth) {
                throw new UnsupportedOperationException("Root does not support this operation");
            }
        };

        OntologyTermWrapper otwRoot = new OntologyTermWrapper(root, interactorIndexSearcher, baseQuery);

        setWrappedData(otwRoot);
    }

    @Override
    protected Object getChildData(Object parentData) {
        List<OntologyTermWrapper> children = (List<OntologyTermWrapper>) super.getChildData(parentData);

        OntologyTermWrapper parent = (OntologyTermWrapper) parentData;

        String childrenInteractorColour = null;

        if (parent.getInteractorColour() == null) {
            childrenInteractorColour = interactorColours[0];
        } else {
            childrenInteractorColour = nextColour(interactorColours, parent.getInteractorColour());
        }

        for (OntologyTermWrapper child : children) {
           child.setInteractorColour(childrenInteractorColour); 
        }

        return children;
    }

    private String nextColour(String[] colourArray, String interactorColour) {
        for (int i=0; i<colourArray.length; i++) {
            if (interactorColour.equals(colourArray[i])) {
                if (i+1<colourArray.length) {
                    return colourArray[i+1];
                } else {
                    return colourArray[0];
                }
            }
        }
        return colourArray[0];
    }
}
