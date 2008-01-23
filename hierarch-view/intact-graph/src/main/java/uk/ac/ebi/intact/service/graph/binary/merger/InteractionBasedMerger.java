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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.processor.ClusterInteractorPairProcessor;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetwork;
import uk.ac.ebi.intact.service.graph.binary.BinaryGraphNetworkBuilder;
import uk.ac.ebi.intact.service.graph.binary.BinaryInteractionEdge;
import uk.ac.ebi.intact.service.graph.binary.label.LabelStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id$
 */
public class InteractionBasedMerger implements BinaryGraphNetworkMerger {

    private static final Log logger = LogFactory.getLog( InteractionBasedMerger.class );

    private LabelStrategy labelStrategy;

    public InteractionBasedMerger() {
    }

    public LabelStrategy getLabelStrategy() {
        return labelStrategy;
    }

    public void setLabelStrategy( LabelStrategy labelStrategy ) {
        this.labelStrategy = labelStrategy;
    }

    public BinaryGraphNetwork mergeGraphNetworks( BinaryGraphNetwork network1, BinaryGraphNetwork network2 ) {

        Collection<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();
        Set<String> centralAcs = new HashSet<String>();
        for ( BinaryInteraction bi : network1.getBinaryInteractions() ) {
            if ( bi != null && !binaryInteractions.contains( bi ) ) {
                binaryInteractions.add( bi );
            }
        }
        if ( network1.getCentralNodes() != null ) {
            for ( Node node : network1.getCentralNodes() ) {
                if ( !centralAcs.contains( node.getId() ) ) {
                    centralAcs.add( node.getId() );
                }
            }
        }

        for ( BinaryInteraction bi : network2.getBinaryInteractions() ) {
            if ( bi != null && !binaryInteractions.contains( bi ) ) {
                binaryInteractions.add( bi );
            }
        }

        if ( network2.getCentralNodes() != null ) {
            for ( Node node : network2.getCentralNodes() ) {
                if ( !centralAcs.contains( node.getId() ) ) {
                    centralAcs.add( node.getId() );
                }
            }
        }

        if ( !binaryInteractions.isEmpty() ) {
            ClusterInteractorPairProcessor cluster = new ClusterInteractorPairProcessor();
            cluster.setColumnHandler( new IntActColumnHandler() );
            binaryInteractions = cluster.process( binaryInteractions );

            BinaryGraphNetworkBuilder builder = new BinaryGraphNetworkBuilder();
            builder.setLabelStrategy( labelStrategy );
            if ( !centralAcs.isEmpty() ) {
                return builder.createGraphNetwork( binaryInteractions, centralAcs );
            } else {
                return builder.createGraphNetwork( binaryInteractions );
            }
        }

        return null;
    }
}
