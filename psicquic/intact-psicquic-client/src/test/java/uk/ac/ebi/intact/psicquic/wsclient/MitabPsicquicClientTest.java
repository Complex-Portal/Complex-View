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

import org.junit.Test;
import org.junit.Ignore;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import psidev.psi.mi.search.SearchResult;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class MitabPsicquicClientTest {

    @Test
    public void client() throws Exception {
        MitabPsicquicClient client = new MitabPsicquicClient("http://www.ebi.ac.uk/intact/psicquic/webservices/psicquic");

        SearchResult<IntactBinaryInteraction> searchResult = client.getByInteractor("brca2", 0, 50);
        
        for (IntactBinaryInteraction ibi : searchResult.getData()) {
            System.out.println(ibi.getInteractionAcs());
        }
    }
}
