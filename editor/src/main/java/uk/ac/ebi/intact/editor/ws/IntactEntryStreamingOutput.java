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

import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.tab2graphml.Tab2Cytoscapeweb;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.stylesheets.XslTransformerUtils;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabWriter;
import uk.ac.ebi.intact.psimitab.IntactXml2Tab;

import javax.persistence.FlushModeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class IntactEntryStreamingOutput implements StreamingOutput {

    private String format;

    public IntactEntryStreamingOutput(String format) {
        this.format = format;
    }

    public abstract IntactEntry createIntactEntry();

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getEntityManager().setFlushMode(FlushModeType.COMMIT);

        try {
            if (MiExportService.FORMAT_XML254.equals(format)) {
                writeXml(outputStream);
            } else if (MiExportService.FORMAT_MITAB25.equals(format)) {
                writeTab25(outputStream);
            } else if (MiExportService.FORMAT_MITAB25_INTACT.equals(format)) {
                writeTab25Intact(outputStream);
            } else if (MiExportService.FORMAT_HTML.equals(format)) {
                writeHtml(outputStream);
            } else if (MiExportService.FORMAT_JSON.equals(format)) {
                writeJson(outputStream);
            } else if (MiExportService.FORMAT_GRAPHML.equals(format)){
                writeGraphml(outputStream);
            }
        } catch (Throwable e) {
            throw new IOException(e);
        } finally {
            IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().clear();
            IntactContext.getCurrentInstance().getDataContext().rollbackTransaction(transactionStatus);
        }

    }

    public void writeXml(OutputStream outputStream) throws IOException, WebApplicationException {
         EntrySet entrySet = createEntrySet(createIntactEntry());

           PsimiXmlWriter writer = new PsimiXmlWriter(PsimiXmlVersion.VERSION_254);

           try {
               writer.write(entrySet, outputStream);
           } catch (Exception e) {
               throw new IOException("Export failed, problem exporting to PSI-MI XML: ", e);
           }
    }

    public void writeTab25(OutputStream outputStream) throws IOException, WebApplicationException {
         exportToMitab(new PsimiTabWriter(false), outputStream);
    }

    public void writeTab25Intact(OutputStream outputStream) throws IOException, WebApplicationException {
        IntactPsimiTabWriter psimitabWriter = new IntactPsimiTabWriter();
        psimitabWriter.setHeaderEnabled(false);

        exportToMitab(psimitabWriter, outputStream);
    }

    public void writeHtml(OutputStream outputStream) throws IOException, WebApplicationException {
        EntrySet entrySet = createEntrySet(createIntactEntry());

        PsimiXmlWriter writer = new PsimiXmlWriter(PsimiXmlVersion.VERSION_254);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            writer.write(entrySet, baos);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toString().getBytes());

            XslTransformerUtils.viewPsiMi25(bais, outputStream);
            //transform(os, bais, BinaryInteractionsExporter.class.getResourceAsStream("/META-INF/MIF254_view.xsl"));
        }  catch ( Exception e ) {
            throw new IOException("Problem converting to HTML", e);
        }
    }

    public void writeJson(OutputStream outputStream) throws IOException {
        EntrySet entrySet = createEntrySet(createIntactEntry());
        PsimiXmlWriter writer = new PsimiXmlWriter(PsimiXmlVersion.VERSION_254);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            writer.write(entrySet, baos);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toString().getBytes());

            XslTransformerUtils.jsonPsiMi(bais, outputStream);
        } catch (Exception e) {
            throw new IOException("Problem converting to JSON", e);
        }
    }

    public void writeGraphml(OutputStream outputStream) throws IOException {
        OutputStream miStream = new ByteArrayOutputStream();
        writeTab25(miStream);

        ByteArrayInputStream bais = new ByteArrayInputStream(miStream.toString().getBytes());

        final Tab2Cytoscapeweb tab2Cytoscapeweb = new Tab2Cytoscapeweb();
        String output = null;
        try {
            output = tab2Cytoscapeweb.convert(bais);
        } catch (ConverterException e) {
            throw new IllegalStateException( "Could not parse input MITAB.", e );
        }

        outputStream.write(output.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    private EntrySet createEntrySet(IntactEntry intactEntry) {
//        final TransactionStatus transactionStatus = getIntactContext().getDataContext().beginTransaction();
//        getIntactContext().getDataContext().getDaoFactory().getEntityManager().setFlushMode(FlushModeType.COMMIT);

        // This is the main method to export data. An EntrySet is the equivalent to the IntactEntry object
        // but is a member of the PSI-MI model (IntactEntry is for the Intact model)
        PsiExchange psiExchange = (PsiExchange) IntactContext.getCurrentInstance().getSpringContext().getBean("psiExchange");
        EntrySet entrySet = psiExchange.exportToEntrySet(intactEntry);

        // we rollback as we don't need to commit any change
//        getIntactContext().getDataContext().rollbackTransaction(transactionStatus);

        return entrySet;
    }

       public void exportToMitab(PsimiTabWriter psimitabWriter, OutputStream outputStream) throws IOException {
           EntrySet entrySet = createEntrySet(createIntactEntry());

           // Setup a interaction expansion strategy that is going to transform n-ary interactions into binaries using
           // the spoke expansion algorithm
           IntactXml2Tab xml2tab = new IntactXml2Tab();
           xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );

           try {
               Collection<BinaryInteraction> binaryInteractions = xml2tab.convert(entrySet);

               psimitabWriter.setHeaderEnabled(false);

               psimitabWriter.write(binaryInteractions, outputStream);

           } catch (Exception e) {
               throw new IOException("Problem exporting to MITAB: ",e);
           }

       }

    public IntactContext getIntactContext() {
        return IntactContext.getCurrentInstance();
    }
}
