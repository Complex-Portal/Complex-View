/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.service.graph.binary;

import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.service.graph.Node;

import java.util.Collection;
import java.util.HashSet;

import edu.uci.ics.jung.graph.impl.SimpleSparseVertex;
import edu.uci.ics.jung.utils.UserData;

/**
 * Implementation of Vertex that holds the interactor information
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorVertex extends SimpleSparseVertex implements Node<BinaryInteractionEdge> {

    private Interactor interactor;
    private Collection<BinaryInteractionEdge> edges;

    protected InteractorVertex(Interactor interactor) {
        this.interactor = interactor;

        addUserDatum("id", getId(), UserData.SHARED);
    }

    public Collection<BinaryInteractionEdge> getEdges() {
        if (edges == null) {
            edges = new HashSet<BinaryInteractionEdge>();
        }
        return edges;
    }

    public String getId() {
        return interactor.getIdentifiers().iterator().next().getIdentifier();
    }

    public Interactor getInteractor() {
        return interactor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        InteractorVertex that = (InteractorVertex) o;

        if (interactor != null ? !interactor.equals(that.interactor) : that.interactor != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (interactor != null ? interactor.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getId()+getEdges();
    }
}