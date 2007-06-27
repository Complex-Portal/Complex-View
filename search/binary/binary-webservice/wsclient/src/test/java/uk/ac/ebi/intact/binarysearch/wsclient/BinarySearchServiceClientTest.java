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
import uk.ac.ebi.intact.binarysearch.wsclient.generated.BinarySearch;
import uk.ac.ebi.intact.binarysearch.wsclient.generated.SearchResult;

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

        BinarySearch port = client.getBinarySearchPort();

        SearchResult sr = port.findBinaryInteractions("P12345");

        System.out.println(sr.getInteractions().size());

        System.out.println(client.findBinaryInteractionsByIdentifiers("P12345", "P34567").getTotalCount());

    }
}