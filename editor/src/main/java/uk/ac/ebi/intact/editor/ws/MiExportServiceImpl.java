/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.ws;

import org.primefaces.model.StreamedContent;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.entry.IntactEntryFactory;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabWriter;
import uk.ac.ebi.intact.psimitab.IntactXml2Tab;

import javax.faces.event.ActionEvent;
import javax.persistence.FlushModeType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.util.Collection;

/**
 * TODO comment this class header.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class MiExportServiceImpl implements MiExportService {


    public Object exportPublication(final String ac, final String format) {
        Response response = null;
        try {
            String responseType = calculateResponseType(format);

            StreamingOutput output = new IntactEntryStreamingOutput(format) {
                @Override
                public IntactEntry createIntactEntry() {
                    return IntactEntryFactory.createIntactEntry(IntactContext.getCurrentInstance())
                            .addPublicationWithAc(ac);
                }
            };

            response = Response.status(200).type(responseType).entity(output).build();
        } catch (Throwable e) {
            throw new RuntimeException("Problem exporting publication: "+ac, e);
        }

        return response;
    }

    public Object exportExperiment(final String ac, final String format) {
        Response response = null;
        try {
            String responseType = calculateResponseType(format);

            StreamingOutput output = new IntactEntryStreamingOutput(format) {
                @Override
                public IntactEntry createIntactEntry() {
                    return IntactEntryFactory.createIntactEntry(IntactContext.getCurrentInstance())
                            .addExperimentWithAc(ac);
                }
            };

            response = Response.status(200).type(responseType).entity(output).build();
        } catch (Throwable e) {
            throw new RuntimeException("Problem exporting experiment: "+ac, e);
        }

        return response;
    }

    public Object exportInteraction(final String ac, final String format) {
        Response response = null;
        try {
            String responseType = calculateResponseType(format);

            StreamingOutput output = new IntactEntryStreamingOutput(format) {
                @Override
                public IntactEntry createIntactEntry() {
                    return IntactEntryFactory.createIntactEntry(IntactContext.getCurrentInstance())
                            .addInteractionWithAc(ac);
                }
            };

            response = Response.status(200).type(responseType).entity(output).build();
        } catch (Throwable e) {
            throw new RuntimeException("Problem exporting interaction: "+ac, e);
        }

        return response;
    }

    private String calculateResponseType(String format) {
        String responseType;

        if (format.contains("xml")) {
            responseType = "application/xml";
        } else if (format.contains("tab")) {
            responseType = "text/plain";
        } else if (format.contains("html")) {
            responseType = "text/html";
        } else {
            throw new IllegalArgumentException("Unexpected format: "+format);
        }
        return responseType;
    }


}
