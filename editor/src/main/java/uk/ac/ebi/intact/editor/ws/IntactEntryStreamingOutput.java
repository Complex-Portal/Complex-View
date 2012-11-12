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
import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.tab2graphml.Tab2Cytoscapeweb;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;
import psidev.psi.mi.xml.PsimiXmlForm;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.stylesheets.XslTransformerUtils;
import uk.ac.ebi.intact.core.context.IntactContext;

import javax.persistence.FlushModeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
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

    public abstract Object createIntactEntry();

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getEntityManager().setFlushMode(FlushModeType.COMMIT);

        try {
            if (MiExportService.FORMAT_XML254.equals(format)) {
                writeXml(outputStream);
            } else if (MiExportService.FORMAT_MITAB25.equals(format)) {
                writeTab25(outputStream);
            } else if (MiExportService.FORMAT_MITAB26.equals(format)) {
                writeTab26(outputStream);
            }else if (MiExportService.FORMAT_MITAB27.equals(format)) {
                writeTab27(outputStream);
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
        Object obj = createIntactEntry();
        if (obj instanceof EntrySet){
            EntrySet entrySet = (EntrySet) createIntactEntry();

            PsimiXmlWriter writer = new PsimiXmlWriter(PsimiXmlVersion.VERSION_254, PsimiXmlForm.FORM_COMPACT);

            try {
                writer.write(entrySet, outputStream);
            } catch (Exception e) {
                throw new IOException("Export failed, problem exporting to PSI-MI XML: ", e);
            }
        }
        else {
            throw new IOException("Export failed, problem exporting to PSI-MI XML. It was expecting an EntrySet when creating Intact entry.");
        }
    }

    public void writeTab25(OutputStream outputStream) throws IOException, WebApplicationException {
        exportToMitab(new PsimiTabWriter(PsimiTabVersion.v2_5), outputStream);
    }
    public void writeTab26(OutputStream outputStream) throws IOException, WebApplicationException {
        exportToMitab(new PsimiTabWriter(PsimiTabVersion.v2_6), outputStream);
    }
    public void writeTab27(OutputStream outputStream) throws IOException, WebApplicationException {
        exportToMitab(new PsimiTabWriter(PsimiTabVersion.v2_7), outputStream);
    }

    public void writeHtml(OutputStream outputStream) throws IOException, WebApplicationException {
        Object obj = createIntactEntry();
        if (obj instanceof EntrySet){
            EntrySet entrySet = (EntrySet) createIntactEntry();

            PsimiXmlWriter writer = new PsimiXmlWriter(PsimiXmlVersion.VERSION_254, PsimiXmlForm.FORM_COMPACT);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                writer.write(entrySet, baos);

                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toString().getBytes());

                try{
                    XslTransformerUtils.viewPsiMi25(bais, outputStream);
                }
                finally {
                    bais.close();
                }
                //transform(os, bais, BinaryInteractionsExporter.class.getResourceAsStream("/META-INF/MIF254_view.xsl"));
            }  catch ( Exception e ) {
                throw new IOException("Problem converting to HTML", e);
            }
            finally {
                baos.close();
            }
        }
        else {
            throw new IOException("Export failed, problem exporting to PSI-MI HTML. It was expecting an EntrySet when creating Intact entry.");
        }
    }

    public void writeJson(OutputStream outputStream) throws IOException {
        Object obj = createIntactEntry();
        if (obj instanceof EntrySet){
            EntrySet entrySet = (EntrySet) createIntactEntry();

            PsimiXmlWriter writer = new PsimiXmlWriter(PsimiXmlVersion.VERSION_254, PsimiXmlForm.FORM_COMPACT);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                writer.write(entrySet, baos);

                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toString().getBytes());

                try{
                    XslTransformerUtils.jsonPsiMi(bais, outputStream);
                }
                finally {
                    bais.close();
                }
                //transform(os, bais, BinaryInteractionsExporter.class.getResourceAsStream("/META-INF/MIF254_view.xsl"));
            }  catch ( Exception e ) {
                throw new IOException("Problem converting to JSON", e);
            }
            finally {
                baos.close();
            }
        }
        else {
            throw new IOException("Export failed, problem exporting to JSON. It was expecting an EntrySet when creating Intact entry.");
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
        } catch (PsimiTabException e) {
            throw new IllegalStateException( "Could not parse input MITAB.", e );
        }finally {
            bais.close();
            miStream.close();
        }

        outputStream.write(output.getBytes());
        outputStream.flush();
        outputStream.close();

    }

    public void exportToMitab(PsimiTabWriter psimitabWriter, OutputStream outputStream) throws IOException {
        Object obj = createIntactEntry();
        if (obj instanceof Collection){
            Collection<BinaryInteraction> binaryInteractions = (Collection<BinaryInteraction>) createIntactEntry();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            try {
                psimitabWriter.writeMitabHeader(writer);
                psimitabWriter.write(binaryInteractions, writer);

                //transform(os, bais, BinaryInteractionsExporter.class.getResourceAsStream("/META-INF/MIF254_view.xsl"));
            }  catch ( Exception e ) {
                throw new IOException("Problem converting to MITAB 2.5", e);
            }
            finally {
                writer.close();
            }
        }
        else {
            throw new IOException("Export failed, problem exporting to MITAB 2.5. It was expecting a Collection of BinaryInteractions when creating Intact entry.");
        }
    }

    public IntactContext getIntactContext() {
        return IntactContext.getCurrentInstance();
    }
}
