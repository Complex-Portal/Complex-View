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
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.myfaces.trinidad.event.RowDisclosureEvent;
import org.apache.myfaces.trinidad.event.FocusEvent;
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

    private String[] INTERACTOR_COLOURS = new String[] {"#73b360", "#84bc73", "#96c688", "#a7cf9b","#cae2c3", "#dcecd7"};
    private String[] INTERACTION_COLOURS = new String[] {"#006666", "#1f7979", "#408c8c", "#5e9e9e","#a1c7c7", "#bdd7d7"};

    private IndexSearcher interactionIndexSearcher;
    private IndexSearcher interactorIndexSearcher;
    private String baseQuery;

    public GoOntologyTreeModel(final OntologyIndexSearcher ontologyIndexSearcher,
                               final IndexSearcher interactionIndexSearcher,
                               final IndexSearcher interactorIndexSearcher,
                               final String baseQuery) {
        setChildProperty("children");

        this.interactionIndexSearcher = interactionIndexSearcher;
        this.interactorIndexSearcher = interactorIndexSearcher;
        this.baseQuery = baseQuery;

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

        OntologyTermWrapper otwRoot = new OntologyTermWrapper(root, interactionIndexSearcher, interactorIndexSearcher, baseQuery);

        setWrappedData(otwRoot);
    }

    @Override
    protected Object getChildData(Object parentData) {
        //List<OntologyTermWrapper> children = (List<OntologyTermWrapper>) super.getChildData(parentData);
        List<OntologyTermWrapper> children = new ArrayList<OntologyTermWrapper>();

        OntologyTermWrapper parent = (OntologyTermWrapper) parentData;

        String childrenInteractorColour = calculateNextColour(parent);

        int childrenInteractorTotalCount = 0;
        int childrenInteractionTotalCount = 0;

        for (OntologyTerm child : parent.getTerm().getChildren()) {
            OntologyTermWrapper otwChild = new OntologyTermWrapper(child, interactionIndexSearcher, interactorIndexSearcher, baseQuery, false);

            //if (otwChild.getInteractorCount() > 0) {
                otwChild.setInteractorColour(childrenInteractorColour);

                children.add(otwChild);
                otwChild.setParent(parent);
                childrenInteractionTotalCount = childrenInteractorTotalCount + otwChild.getInteractorCount();
                childrenInteractionTotalCount = childrenInteractionTotalCount + otwChild.getInteractionCount();
            //}

            parent.setChildrenInteractorTotalCount(childrenInteractorTotalCount);
            parent.setChildrenInteractionTotalCount(childrenInteractionTotalCount);
        }

        Collections.sort(children, new OntologyTermWrapperComparator());

        return children;
    }

    private String calculateNextColour(OntologyTermWrapper parent) {
        String childrenInteractorColour = null;

        if (parent.getInteractorColour() == null) {
            childrenInteractorColour = INTERACTOR_COLOURS[0];
        } else {
            childrenInteractorColour = nextColour(INTERACTOR_COLOURS, parent.getInteractorColour());
        }
        return childrenInteractorColour;
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

    private class OntologyTermWrapperComparator implements Comparator<OntologyTermWrapper> {

        public int compare(OntologyTermWrapper o1, OntologyTermWrapper o2) {
            if (o1.getInteractorCount() > o2.getInteractorCount()) {
                return -1;
            } else {
                return +1;
            }
        }
    }
}
