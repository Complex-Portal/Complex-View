/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
 * limitations under the License.
 */
package uk.ac.ebi.intact.psicquic.wsclient;

import org.hupo.psi.mi.psicquic.wsclient.QueryOperand;
import org.junit.Test;
import psidev.psi.mi.search.SearchResult;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class MitabPsicquicClientTest {

    @Test
    public void client() throws Exception {
        MitabPsicquicClient client = new MitabPsicquicClient("http://localhost:9090/intact-psicquic-ws/webservices/psicquic");

        SearchResult<IntactBinaryInteraction> searchResult = client.getByInteractorList(new String[] {"Q9VXG8", "P38111"}, QueryOperand.OR, 0, 50);
        
        for (IntactBinaryInteraction ibi : searchResult.getData()) {
            System.out.println(ibi.getInteractionAcs());
        }
    }
}
