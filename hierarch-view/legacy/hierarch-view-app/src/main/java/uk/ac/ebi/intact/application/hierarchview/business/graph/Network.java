/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.business.graph;

import psidev.psi.mi.tab.model.Author;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageDimension;
import uk.ac.ebi.intact.searchengine.CriteriaBean;
import uk.ac.ebi.intact.service.graph.Edge;
import uk.ac.ebi.intact.service.graph.GraphNetwork;
import uk.ac.ebi.intact.service.graph.Node;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public interface Network {

    Author getAuthorByPMID( String pmid );

    CrossReference getCrossReferenceById( String id );

    NodeAttributes getNodeAttributes( Node node );

    EdgeAttributes getEdgeAttributes( Edge edge );

    Collection<BinaryInteraction> getBinaryInteraction();

    GraphNetwork getGraphNetwork();

    Map getNodeHighlightMap();

    Map getEdgeHighlightMap();

    void setBinaryInteractions( Collection<BinaryInteraction> binaryInteractions );

    Collection<CrossReference> getProperties( Node node );

    boolean isNodeHighlightMapEmpty();

    boolean isEdgeHighlightMapEmpty();

    void initHighlightMap();

    int getDatabaseTermCount( String source, String TermId );

    Set<Node> getNodesForHighlight( String source, String sourceID );

    Set<Edge> getEdgesForHighlight( String source, String sourceID );

    List<Node> getCentralNodes();

    List getCriteria();

    void addCriteria( CriteriaBean aCriteria );

    void initNodes();

    void initEdges();

    ImageDimension getImageDimension();

    String exportTlp();

    String exportJavascript( float rateX, float rateY, int borderSize );

    String[] importDataToImage( String dataTlp ) throws RemoteException;

    Collection<Edge> getEdges();

    List<Node> getNodes();
}
