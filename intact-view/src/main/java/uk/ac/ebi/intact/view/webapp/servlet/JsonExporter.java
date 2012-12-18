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
package uk.ac.ebi.intact.view.webapp.servlet;

import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.stylesheets.XslTransformException;
import psidev.psi.mi.xml.stylesheets.XslTransformerUtils;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.entry.IntactEntryFactory;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.IntactEntry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Outputs JSON corresponding to an interaction.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */

public class JsonExporter extends HttpServlet {

    private static final String NEW_LINE = System.getProperty("line.separator");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String interactionAc = req.getParameter("ac");

        if (interactionAc == null) {
            throw new ServletException("Parameter 'ac' was expected");
        }

        InputStream xmlStream = createXmlStream(interactionAc);

        try {
            XslTransformerUtils.jsonPsiMi(xmlStream, resp.getOutputStream());
        } catch (XslTransformException e) {
            throw new ServletException("Problem writing JSON", e);
        }
        finally {
            xmlStream.close();
        }
    }

    private InputStream createXmlStream(String interactionAc) throws ServletException {
        EntrySet entrySet = createEntrySet(interactionAc);

        PsimiXmlWriter writer = new PsimiXmlWriter(PsimiXmlVersion.VERSION_254);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            writer.write(entrySet, baos);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());

            baos.close();
            return inputStream;
        } catch (Exception e) {
            throw new ServletException("Problem writing XML", e);
        }
    }

    private EntrySet createEntrySet(String interactionAc) {
        IntactContext context = IntactContext.getCurrentInstance();

        TransactionStatus status = context.getDataContext().beginTransaction();
        //context.getDaoFactory().getEntityManager().setFlushMode(FlushModeType.COMMIT);
        IntactEntry intactEntry = IntactEntryFactory.createIntactEntry(context)
                .addInteractionWithAc(interactionAc);

        EntrySet entrySet = createEntrySet(intactEntry);
        context.getDataContext().rollbackTransaction(status);

        return entrySet;
    }

    private EntrySet createEntrySet(IntactEntry intactEntry) {
        PsiExchange psiExchange = (PsiExchange) IntactContext.getCurrentInstance().getSpringContext().getBean("psiExchange");
        EntrySet entrySet = null;
        try{
            entrySet = psiExchange.exportToEntrySet(intactEntry);
        }
        finally {
            psiExchange.close();
        }

        return entrySet;
    }
}
