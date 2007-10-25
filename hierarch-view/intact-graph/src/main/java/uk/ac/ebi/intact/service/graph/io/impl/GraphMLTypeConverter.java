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
package uk.ac.ebi.intact.service.graph.io.impl;

import uk.ac.ebi.intact.service.graph.Edge;
import uk.ac.ebi.intact.service.graph.GraphNetwork;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.service.graph.io.GraphNetworkConverter;
import uk.ac.ebi.intact.service.graph.jaxb.*;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class GraphMLTypeConverter implements GraphNetworkConverter<GraphmlType>{

    public GraphmlType from(GraphNetwork<? extends Node, ? extends Edge> graphNetwork) {
        GraphmlType graphml = new GraphmlType();

        GraphType graph = new GraphType();
        graph.setId("graph_"+System.currentTimeMillis());
        graph.setEdgedefault(GraphEdgedefaultType.UNDIRECTED);

        graphml.getGraphOrData().add(graph);


        for (Node node : (Collection<Node>) graphNetwork.getNodes()) {
            NodeType nodeType = new NodeType();
            nodeType.setId(node.getId());

            graph.getDataOrNodeOrEdge().add(nodeType);

            for (Edge edge : (Collection<Edge>) node.getEdges()) {
                EdgeType edgeType = new EdgeType();
                edgeType.setSource(edge.getNodeA().getId());
                edgeType.setTarget(edge.getNodeB().getId());

                graph.getDataOrNodeOrEdge().add(edgeType);
            }
        }

        return graphml;
    }

    public GraphNetwork<? extends Node, ? extends Edge> to(GraphmlType source) {
        throw new UnsupportedOperationException();
    }
}