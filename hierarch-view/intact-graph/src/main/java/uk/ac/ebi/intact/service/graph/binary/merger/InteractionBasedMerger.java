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
package uk.ac.ebi.intact.service.graph.binary.merger;

import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetwork;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetworkBuilder;
import uk.ac.ebi.intact.service.graph.binary.BinaryInteractionEdge;

import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id$
 */
public class InteractionBasedMerger implements BinaryGraphNetworkMerger {

    public BinaryGraphNetwork mergeGraphNetworks( BinaryGraphNetwork network1, BinaryGraphNetwork network2 ) {

        Collection<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();
        Collection<String> centralAcs = new ArrayList<String>();
        for ( Object e : network1.getEdges()){
            BinaryInteraction bi = ((BinaryInteractionEdge) e).getBinaryInteraction();
            if (bi != null){
                binaryInteractions.add(bi);
            }
        }
        if (network1.getCentralNodes() != null) {
            for ( Node node :network1.getCentralNodes()){
                centralAcs.add(node.getId());
            }
        }

        for ( Object e : network2.getEdges()){
            BinaryInteraction bi = ((BinaryInteractionEdge) e).getBinaryInteraction();
            if (bi != null){
                binaryInteractions.add(bi);
            }
        }

        if (network2.getCentralNodes() != null) {
            for ( Node node : network2.getCentralNodes()){
                centralAcs.add(node.getId());
            }
        }

        if (!binaryInteractions.isEmpty()){
            BinaryGraphNetworkBuilder builder = new BinaryGraphNetworkBuilder();
            if (!centralAcs.isEmpty()) {
                return builder.createGraphNetwork(binaryInteractions, centralAcs);
            } else {
                return builder.createGraphNetwork( binaryInteractions);
            }
        }

        return null;
    }
}
