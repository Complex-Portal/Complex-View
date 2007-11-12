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
package uk.ac.ebi.intact.binarysearch.wsclient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import psidev.psi.mi.search.SearchResult;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;

import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BinarySearchServiceClientTest {

    private static final String LOCAHOST_URL = "http://localhost:24521/ws/binarysearch?wsdl";

    private BinarySearchServiceClient client;

    @Before
    public void setUp() throws Exception {
        client = new BinarySearchServiceClient(LOCAHOST_URL);
    }

    @After
    public void tearDown() throws Exception {
        client = null;
    }

    @Test
    public void testPort() throws Exception {


        SearchResult sr = client.findBinaryInteractions("brca2");

        List<IntActBinaryInteraction> interactions = sr.getInteractions();

        for (IntActBinaryInteraction ibi : interactions) {
            System.out.println(ibi.getInteractorA().getIdentifiers()+" - "+ibi.getInteractorB().getIdentifiers());
        }

        System.out.println(sr.getInteractions().size());

        System.out.println(client.findBinaryInteractionsByIdentifiers("P12345", "P34567").getTotalCount());

    }
}