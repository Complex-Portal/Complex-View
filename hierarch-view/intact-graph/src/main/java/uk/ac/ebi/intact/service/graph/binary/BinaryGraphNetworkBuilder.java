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

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.UndirectedSparseVertex;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.service.graph.Edge;
import uk.ac.ebi.intact.service.graph.GraphNetwork;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.service.graph.GraphException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Creates <code>BinaryGraphNetwork</code>s
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryGraphNetworkBuilder {

    public BinaryGraphNetworkBuilder() {
    }

    /**
     * This methods creates a BinaryGraphNetwork from a collection of BinaryInteractions.
     * It identifies commons interactors and creates the necessary edges
     *
     * @param binaryInteractions
     *
     * @return BinaryGraphNetwork
     *
     * @throws Exception
     */
    public BinaryGraphNetwork createGraphNetwork(Collection<BinaryInteraction> binaryInteractions) {
        BinaryGraphNetwork graphNetwork = new BinaryGraphNetwork();

        for (BinaryInteraction interaction : binaryInteractions) {
            InteractorVertex vertexA = createVertex(interaction.getInteractorA(), graphNetwork);
            InteractorVertex vertexB = createVertex(interaction.getInteractorB(), graphNetwork);

            BinaryInteractionEdge edge = new BinaryInteractionEdge(interaction, vertexA, vertexB);
            graphNetwork.addEdge(edge);
        }

        return graphNetwork;
    }

    protected static InteractorVertex createVertex(Interactor interactor, BinaryGraphNetwork graph) {
        InteractorVertex vertex = new InteractorVertex(interactor);
        InteractorVertex existingVertex = graph.findNode(vertex.getId());

        if (existingVertex != null) {
            return existingVertex;
        }

        graph.addNode(vertex);

        StringLabeller labeler = StringLabeller.getLabeller(graph);
        try {
            labeler.setLabel(vertex, vertex.getId());
        } catch (StringLabeller.UniqueLabelException e) {
            throw new GraphException("Problem setting the label to vertex: "+vertex, e);
        }
        

        return vertex;
    }



}