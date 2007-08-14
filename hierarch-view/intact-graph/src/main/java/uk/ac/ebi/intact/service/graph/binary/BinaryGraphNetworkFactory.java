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

import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.Collection;

/**
 * Creates <code>BinaryGraphNetwork</code>s
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryGraphNetworkFactory {

    private BinaryGraphNetworkFactory() {}

    /**
     * This methods creates a BinaryGraphNetwork from a collection of BinaryInteractions.
     * It identifies commons interactors and creates the necessary edges
     * @param binaryInteractions
     * @return
     */
    public static BinaryGraphNetwork createBinaryGraphNetwork(Collection<BinaryInteraction> binaryInteractions) {

        // the algorithm is a progressive algorigthm where we go checking each node
        // for the provided interactions and create new Nodes/Edges with the partners.
        // when an interation is done for a node, do the next node, and so on
        return null;
    }
}