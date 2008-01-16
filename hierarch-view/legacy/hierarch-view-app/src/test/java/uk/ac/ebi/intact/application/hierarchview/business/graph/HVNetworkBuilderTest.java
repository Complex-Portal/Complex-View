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

import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.ebi.intact.application.hierarchview.business.data.DataService;
import uk.ac.ebi.intact.application.hierarchview.business.data.DataServiceMock;
import uk.ac.ebi.intact.service.graph.Node;

/**
 * HVNetworkBuilder Test.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class HVNetworkBuilderTest {

    @Test
    public void buildBinaryGraphNetworkTest() throws Exception {
        DataService dataservice = new DataServiceMock();
        HVNetworkBuilder builder = new HVNetworkBuilder( dataservice );

        Network network = builder.buildBinaryGraphNetwork( "brca2" );

        assertNotNull( network );
        assertEquals( 2, network.getCentralNodes().size() );
        Node centralNode1 = network.getCentralNodes().get( 0 );
        assertTrue( centralNode1.isCentralNode() );
        Node centralNode2 = network.getCentralNodes().get( 1 );
        assertTrue( centralNode2.isCentralNode() );

        assertEquals( 8, network.getNodes().size() );
        for ( Node node : network.getNodes() ) {
            assertNotNull( node.getId() );
            assertNotNull( node.getLabel() );
        }

        assertEquals( 7, network.getEdges().size() );

        assertEquals( dataservice.getBinaryInteractionsByQueryString( "brca2" ), network.getBinaryInteraction() );
        assertEquals( 27, network.getProperties( centralNode1 ).size() );
        assertEquals( 28, network.getProperties( centralNode2 ).size() );

        network.initHighlightMap();
        assertEquals( 5, network.getNodesForHighlight( "GO", "GO:0005515" ).size() );
        assertEquals( 2, network.getNodesForHighlight( "Interpro", "IPR015188" ).size() );
    }
}
