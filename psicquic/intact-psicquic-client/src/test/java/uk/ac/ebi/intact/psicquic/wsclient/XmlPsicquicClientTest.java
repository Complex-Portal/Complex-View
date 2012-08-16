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

import org.hupo.psi.mi.psicquic.wsclient.XmlPsicquicClient;
import org.hupo.psi.mi.psicquic.wsclient.XmlSearchResult;
import org.junit.Test;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.Interaction;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class XmlPsicquicClientTest {

    @Test
    public void client() throws Exception {
        XmlPsicquicClient client = new XmlPsicquicClient("http://www.ebi.ac.uk/tc-test/intact/psicquic/webservices/psicquic");

        XmlSearchResult searchResult = client.getByInteractor("brca2", 0, 50);

        for (Entry entry : searchResult.getEntrySet().getEntries()) {
            for (Interaction interaction : entry.getInteractions()) {
               System.out.println(interaction);
            }

        }
    }
}