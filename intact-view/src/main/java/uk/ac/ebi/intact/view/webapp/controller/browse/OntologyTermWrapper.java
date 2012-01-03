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

import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.view.webapp.util.RootTerm;

import java.io.Serializable;
import java.util.*;

/**
 * Ontology term wrapper.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyTermWrapper implements Serializable {

    private OntologyTerm term;
    private long interactionCount;

    private Map<String, Long> termsCountMap;
    private boolean showIfEmpty = false;

    private OntologyTermWrapper parent;

    private List<OntologyTermWrapper> children;
    private boolean isLeaf;

    public OntologyTermWrapper(OntologyTerm term, Map<String, Long> termsCountMap, boolean showIfEmpty) {
        this.term = term;
        this.termsCountMap = termsCountMap;
        this.showIfEmpty = showIfEmpty;

        Long count = termsCountMap.get(term.getId());

        if (count != null) {
            interactionCount = count;
        }
    }

    public OntologyTerm getTerm() {
        return term;
    }

    public List<OntologyTermWrapper> getChildren() {
        initChildren();
        return children;
    }

    public boolean isLeaf() {
        initChildren();
        return isLeaf;
    }

    private void initChildren() {
        if (children != null || isLeaf) {
            return;
        }

        children = new ArrayList<OntologyTermWrapper>();

        for (OntologyTerm child : term.getChildren()) {
            OntologyTermWrapper otwChild = new OntologyTermWrapper(child, termsCountMap, showIfEmpty);

            if (showIfEmpty || otwChild.getInteractionCount() > 0 || (term instanceof RootTerm)) {
                children.add(otwChild);
                otwChild.setParent(this);
            }
        }

        Collections.sort(children, new OntologyTermWrapperComparator());

        if (children.isEmpty()) isLeaf = true;
    }

    public OntologyTermWrapper getParent() {
        return parent;
    }

    public void setParent(OntologyTermWrapper parent) {
        this.parent = parent;
    }

    public long getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(int interactionCount) {
        this.interactionCount = interactionCount;
    }



    private class OntologyTermWrapperComparator implements Comparator<OntologyTermWrapper> {

        public int compare(OntologyTermWrapper o1, OntologyTermWrapper o2) {
            if (o1.getInteractionCount() != o2.getInteractionCount()) {
                return Long.valueOf(o2.getInteractionCount()).compareTo(o1.getInteractionCount());
            }
            
            return o1.getTerm().getName().compareTo(o2.getTerm().getName());
        }
    }

    @Override
    public String toString() {
        return "OntologyTermWrapper{"+term.getId()+":"+term.getName()+"}";
    }
}
