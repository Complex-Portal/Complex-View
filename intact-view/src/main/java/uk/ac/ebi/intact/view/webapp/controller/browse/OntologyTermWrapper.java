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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.view.webapp.controller.SearchWebappException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyTermWrapper {

    private OntologyTerm term;
    private int interactionCount;
    private int interactorCount;
    private IndexSearcher interactionIndexSearcher;
    private IndexSearcher interactorIndexSearcher;
    private String baseQuery;
    private String searchQuery;

    private int childrenInteractionTotalCount;
    private int childrenInteractorTotalCount;

    private OntologyTermWrapper parent;

    private List<OntologyTermWrapper> children;

    private String interactionColour;
    private String interactorColour;

    protected OntologyTermWrapper(OntologyTerm term, IndexSearcher interactionIndexSearcher, IndexSearcher interactorIndexSearcher, String baseQuery) {
        this(term, interactionIndexSearcher, interactorIndexSearcher, baseQuery, true);
    }

    protected OntologyTermWrapper(OntologyTerm term, IndexSearcher interactionIndexSearcher, IndexSearcher interactorIndexSearcher, String baseQuery, boolean countInteractors) {
        this.term = term;
        this.interactionIndexSearcher = interactionIndexSearcher;
        this.interactorIndexSearcher = interactorIndexSearcher;
        this.baseQuery = baseQuery;

        if (term == null) throw new NullPointerException("Term is necessary");
        if (interactorIndexSearcher == null) throw new NullPointerException("interactorIndexSearcher is necessary");

//        if (countInteractors) {
//            this.interactionCount = count(term.getId(), interactionIndexSearcher, baseQuery);
//            this.interactorCount = count(term.getId(), interactorIndexSearcher, baseQuery);
//        }
    }

    public OntologyTerm getTerm() {
        return term;
    }

    public int getInteractorCount() {
        return interactorCount;
    }

    public void setInteractorCount(int interactorCount) {
        this.interactorCount = interactorCount;
    }

    public void add(int countToAdd) {
        this.interactorCount = this.interactorCount + countToAdd;
    }

    public List<OntologyTermWrapper> getChildren() {
        if (children != null) {
            return children;
        }

        children = new ArrayList<OntologyTermWrapper>();

        for (OntologyTerm child : term.getChildren()) {
            OntologyTermWrapper otwChild = new OntologyTermWrapper(child, interactionIndexSearcher, interactorIndexSearcher, baseQuery);

            //if (otwChild.getInteractorCount() > 0) {
                children.add(otwChild);
                otwChild.setParent(this);
                childrenInteractionTotalCount = childrenInteractionTotalCount + otwChild.getInteractionCount();
                childrenInteractorTotalCount = childrenInteractorTotalCount + otwChild.getInteractorCount();
            //}
        }

        Collections.sort(children, new OntologyTermWrapperComparator());

        return children;
    }

    public OntologyTermWrapper getParent() {
        return parent;
    }

    public void setParent(OntologyTermWrapper parent) {
        this.parent = parent;
    }

    public int getChildrenInteractorTotalCount() {
        return childrenInteractorTotalCount;
    }

    public void setChildrenInteractorTotalCount(int childrenInteractorTotalCount) {
        this.childrenInteractorTotalCount = childrenInteractorTotalCount;
    }

    public String getInteractorColour() {
        return interactorColour;
    }

    public void setInteractorColour(String interactorColour) {
        this.interactorColour = interactorColour;
    }

    public String getInteractionColour() {
        return interactionColour;
    }

    public void setInteractionColour(String interactionColour) {
        this.interactionColour = interactionColour;
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(int interactionCount) {
        this.interactionCount = interactionCount;
    }

    public int getChildrenInteractionTotalCount() {
        return childrenInteractionTotalCount;
    }

    public void setChildrenInteractionTotalCount(int childrenInteractionTotalCount) {
        this.childrenInteractionTotalCount = childrenInteractionTotalCount;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
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

    @Override
    public String toString() {
        return "OntologyTermWrapper{"+term.getId()+"}";
    }
}
