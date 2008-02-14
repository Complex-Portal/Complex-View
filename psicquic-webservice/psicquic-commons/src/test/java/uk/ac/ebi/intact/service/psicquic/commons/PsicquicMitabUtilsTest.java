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
package uk.ac.ebi.intact.service.psicquic.commons;

import org.junit.Test;
import psidev.psi.mi.search.column.DefaultColumnSet;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.formatter.TabulatedLineFormatter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabReader;
import uk.ac.ebi.intact.service.psicquic.commons.mitab.Mitab;

import java.io.InputStream;
import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsicquicMitabUtilsTest {

    @Test
    public void convertToMitabType_binaryInteractions() throws Exception {
        InputStream mitabStream = PsicquicMitabUtilsTest.class.getResourceAsStream("/mitab/17292829.txt");

        PsimiTabReader reader = new IntactPsimiTabReader(true);
        Collection<BinaryInteraction> binaryInteractions = reader.read(mitabStream);

        Mitab mitab = PsicquicMitabUtils.convertToMitab(binaryInteractions, "intact", new DefaultColumnSet(), new TabulatedLineFormatter());


        /*
        JAXBContext jaxbContext = JAXBContext.newInstance(Mitab.class.getPackage().getName());
        
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(mitab, System.out);
        */
    }
}
