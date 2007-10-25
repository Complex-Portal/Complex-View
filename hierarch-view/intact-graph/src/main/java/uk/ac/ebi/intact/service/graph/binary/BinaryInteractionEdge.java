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

import psidev.psi.mi.tab.model.*;
import uk.ac.ebi.intact.service.graph.Edge;

import java.util.List;

import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.graph.Vertex;

/**
 *
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryInteractionEdge extends UndirectedSparseEdge implements Edge<InteractorVertex> {

    private InteractorVertex vertexA;
    private InteractorVertex vertexB;
    private BinaryInteraction binaryInteraction;

    public BinaryInteractionEdge(BinaryInteraction binaryInteraction, InteractorVertex node1, InteractorVertex node2) {
        super(node1, node2);

        this.binaryInteraction = binaryInteraction;

        this.vertexA = node1;
        this.vertexB = node2;
    }

    //////////////////////////////
    // Implemented methods

    public InteractorVertex getNodeA() {
        return vertexA;
    }

    public InteractorVertex getNodeB() {
        return vertexB;
    }

    public String getId() {
        return getNodeA().getId()+"::"+getNodeB().getId();
    }

    public BinaryInteraction getBinaryInteraction() {
        return binaryInteraction;
    }

    ////////////////////////////////////////////////////////
    // Overriden methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BinaryInteractionEdge that = (BinaryInteractionEdge) o;

        if (vertexA != null ? !vertexA.equals(that.vertexA) : that.vertexA != null) return false;
        if (vertexB != null ? !vertexB.equals(that.vertexB) : that.vertexB != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 31;
        result = 31 * result + (vertexA != null ? vertexA.hashCode() : 0);
        result = 31 * result + (vertexB != null ? vertexB.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getId();
    }

}