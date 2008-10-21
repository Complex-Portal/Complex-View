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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.myfaces.trinidad.model.ChildPropertyTreeModel;
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.myfaces.trinidad.event.RowDisclosureEvent;
import org.apache.myfaces.trinidad.event.FocusEvent;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;
import uk.ac.ebi.intact.bridges.ontologies.term.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.view.webapp.controller.SearchWebappException;

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

    private Set<String> processedTermCounts;

    private IndexSearcher interactionIndexSearcher;
    private IndexSearcher interactorIndexSearcher;
    private String baseQuery;

    private OntologyTermWrapper disclosed;

    public GoOntologyTreeModel(final OntologyIndexSearcher ontologyIndexSearcher,
                               final IndexSearcher interactionIndexSearcher,
                               final IndexSearcher interactorIndexSearcher,
                               final String baseQuery) {
        setChildProperty("children");

        this.interactionIndexSearcher = interactionIndexSearcher;
        this.interactorIndexSearcher = interactorIndexSearcher;
        this.baseQuery = baseQuery;

        processedTermCounts = new HashSet<String>();

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

        updateChildrenCounts(otwRoot);

        setWrappedData(otwRoot);
    }

    @Override
    protected Object getChildData(Object parentData) {
        OntologyTermWrapper parent = (OntologyTermWrapper) parentData;

        List<OntologyTermWrapper> children = new ArrayList<OntologyTermWrapper>();

        String childrenInteractorColour;
        String childrenInteractionColour;

        if (parent.getInteractorColour() == null) {
            childrenInteractorColour = INTERACTOR_COLOURS[0];
            childrenInteractionColour = INTERACTION_COLOURS[0];
        } else {
            childrenInteractorColour = nextColour(INTERACTOR_COLOURS, parent.getInteractorColour());
            childrenInteractionColour = nextColour(INTERACTION_COLOURS, parent.getInteractionColour());
        }

        if (disclosed != null && disclosed.getTerm().getId().equals(parent.getTerm().getId())) {
            updateChildrenCounts(parent);
        } 

        for (OntologyTermWrapper child : parent.getChildren()) {
            //if (child.getInteractionCount() > 0) {
                child.setInteractorColour(childrenInteractorColour);
                child.setInteractionColour(childrenInteractionColour);

                children.add(child);
            //}
        }

        Collections.sort(children, new OntologyTermWrapperComparator());

        return children;
    }

    public void processDisclosure(RowDisclosureEvent evt) {
        if (!evt.getAddedSet().isEmpty()) {
            List<Integer> indexes = (List) evt.getAddedSet().iterator().next();

            Integer[] selected = indexes.toArray(new Integer[indexes.size()]);

            disclosed = getSelectedData(selected);

            //updateChildrenCounts(otw);
        } else {
            disclosed = null;
        }

        processedTermCounts.clear();
    }

    private void updateChildrenCounts(OntologyTermWrapper otw) {
        if (processedTermCounts.contains(otw.getTerm().getId())) {
            return;
        }

        int totalInteractionCount = 0;
        int totalInteractorCount = 0;

        for (OntologyTermWrapper child : otw.getChildren()) {
            String searchQuery = prepareQuery(child.getTerm().getId(), baseQuery);
            child.setSearchQuery(searchQuery);

            int interactionCount = count(searchQuery, interactionIndexSearcher);
            int interactorCount = count(searchQuery, interactorIndexSearcher);

            child.setInteractionCount(interactionCount);
            child.setInteractorCount(interactorCount);

            totalInteractionCount = totalInteractionCount + interactionCount;
            totalInteractorCount = totalInteractorCount + interactorCount;
        }

        otw.setChildrenInteractionTotalCount(totalInteractionCount);
        otw.setChildrenInteractorTotalCount(totalInteractorCount);

        processedTermCounts.add(otw.getTerm().getId());
    }

    private int count(String searchQuery, IndexSearcher indexSearcher)  {

        try {
            long startTime = System.currentTimeMillis();

            Query query = new QueryParser("identifier", new StandardAnalyzer()).parse(searchQuery);
            Hits hits = indexSearcher.search(query);

            System.out.println("Counted: "+searchQuery+" - "+hits.length()+" / Elapsed time: "+(System.currentTimeMillis()-startTime)+" ms");

            int count = hits.length();

            return count;

        } catch (Exception e) {
            throw new SearchWebappException("Problem counting term using query: "+searchQuery, e);
        }
    }

    private String prepareQuery(String id, String baseQuery) {
        return "(" + baseQuery + ") AND properties:\"" + id + "\"";
    }

    private OntologyTermWrapper getSelectedData(Integer[] indexes) {
        OntologyTermWrapper root = (OntologyTermWrapper) getWrappedData();

        List<OntologyTermWrapper> listChildren = (List<OntologyTermWrapper>) getChildData(root);

        OntologyTermWrapper parent = null;

        for (int i=1; i<indexes.length; i++) {
            System.out.println("\tGetting "+indexes[i]+" from "+listChildren);
            parent = listChildren.get(indexes[i]);
            listChildren = (List<OntologyTermWrapper>) getChildData(parent);
        }

        return parent;
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
