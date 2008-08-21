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
package uk.ac.ebi.intact.view.webapp.io;

import org.apache.lucene.search.Sort;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.Searcher;
import psidev.psi.mi.search.engine.SearchEngineException;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.PsimiXmlWriter;
import uk.ac.ebi.intact.psimitab.search.IntactSearchEngine;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabWriter;
import uk.ac.ebi.intact.psimitab.IntactTab2Xml;
import uk.ac.ebi.intact.view.webapp.IntactViewException;

/**
 * Exports to MITAB
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryInteractionsExporter {

    private String luceneIndex;
    private String sortColumn;
    private boolean sortAscendant;

    public BinaryInteractionsExporter(String luceneIndex) {
        this.luceneIndex = luceneIndex;
    }

    public BinaryInteractionsExporter(String luceneIndex, String sortColumn, boolean sortAscendant) {
        this.luceneIndex = luceneIndex;
        this.sortColumn = sortColumn;
        this.sortAscendant = sortAscendant;
    }

    public void searchAndExport(OutputStream os, String searchQuery, String format) throws IOException {
        if ("mitab".equals(format)) {
            exportToMiTab(os, searchQuery);
        } else if ("mitab_intact".equals(format)) {
            exportToMiTabIntact(os, searchQuery);
        } else if ("xml".equals(format)) {
            exportToMiXml(os, searchQuery);
        } else if ("xml_html".equals(format)) {
            exportToMiXmlTransformed(os, searchQuery);
        } else {
            throw new IntactViewException("Format is not correct: " + format + ". Possible values: mitab, mitab_intact.");
        }
    }
    
     public void exportToMiTab(OutputStream os, String searchQuery) throws IOException {
        Writer out = new OutputStreamWriter(os);

        String indexDir = luceneIndex;

        List interactions;
        Integer firstResult = 0;
        Integer maxResults = 50;

        boolean headerEnabled = true;

        do {

            Sort sort = null;

            if (sortColumn != null && sortColumn.length() > 0) {
                sort = new Sort(sortColumn, !sortAscendant);
            }

            SearchResult result = Searcher.search(searchQuery, indexDir, firstResult, maxResults, sort);
            interactions = result.getInteractions();

            PsimiTabWriter writer = new PsimiTabWriter();
            writer.setHeaderEnabled(headerEnabled);
            try {
                writer.write(interactions, out);
            } catch (ConverterException e) {
                throw new IntactViewException("Problem exporting interactions", e);
            }

            headerEnabled = false;

            firstResult = firstResult + maxResults;

            out.flush();

        } while (!interactions.isEmpty());
    }

    private void exportToMiTabIntact(OutputStream os, String searchQuery) throws IOException, IntactViewException {
        Writer out = new OutputStreamWriter(os, "UTF-8");

        String indexDir = luceneIndex;

        List interactions;
        Integer firstResult = 0;
        Integer maxResults = 50;

        boolean headerEnabled = true;

        do {

            Sort sort = null;

            if (sortColumn != null && sortColumn.length() > 0) {
                sort = new Sort(sortColumn, !sortAscendant);
            }

            IntactSearchEngine engine;
            try {
                engine = new IntactSearchEngine(indexDir);
            }
            catch (IOException e) {
                throw new SearchEngineException(e);
            }

            SearchResult<IntactBinaryInteraction> result = engine.search(searchQuery, firstResult, maxResults, sort);
            interactions = result.getInteractions();

            PsimiTabWriter writer = new IntactPsimiTabWriter();
            writer.setHeaderEnabled(headerEnabled);
            try {
                writer.write(interactions, out);
            } catch (ConverterException e) {
                throw new IntactViewException("Problem exporting interactions", e);
            }

            headerEnabled = false;

            firstResult = firstResult + maxResults;

            out.flush();

        } while (!interactions.isEmpty());
    }

    public void exportToMiXml(OutputStream os, String searchQuery) throws IOException {
        Writer out = new OutputStreamWriter(os, "UTF-8");

        IntactSearchEngine engine;
            try {
                engine = new IntactSearchEngine(luceneIndex);
            }
            catch (IOException e) {
                throw new SearchEngineException(e);
            }

        // count first as a security measure
        SearchResult<IntactBinaryInteraction> result1 = engine.search(searchQuery, 0, 1);
        if (result1.getTotalCount() > 1000) {
            throw new IntactViewException("Too many interactions to export to XML. Maximum is 1000");
        }

        SearchResult<IntactBinaryInteraction> result = engine.search(searchQuery, null, null);
        Collection<IntactBinaryInteraction> interactions = result.getInteractions();

        Tab2Xml tab2Xml = new IntactTab2Xml();

        final EntrySet entrySet;
        try {
            entrySet = tab2Xml.convert(new ArrayList<BinaryInteraction>(interactions));
        } catch (Exception e) {
            throw new IntactViewException("Problem converting interactions from MITAB to XML", e);
        }

        PsimiXmlWriter writer = new PsimiXmlWriter();
        try {
            writer.write(entrySet, out);
        } catch (Exception e) {
            throw new IntactViewException("Problem writing XML", e);
        }
    }

    public void exportToMiXmlTransformed(OutputStream os, String searchQuery) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        exportToMiXml(baos, searchQuery);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toString().getBytes());

        try {
            transform(os, bais, BinaryInteractionsExporter.class.getResourceAsStream("/META-INF/MIF25_view.xsl"));
        } catch (TransformerException e) {
            throw new IntactViewException("Problem transforming XML to HTML", e);
        }
    }

    /**
     * The actual method that does the transformation
     * @param isToTransform The stream to transform
     * @param xslt The stream with the XSLT rules
     * @return The transformed stream
     * @throws TransformerException thrown if something has been wrong with the transformation
     */
    private static void transform(OutputStream outputStream, InputStream isToTransform, InputStream xslt) throws TransformerException
    {

        // JAXP reads data using the Source interface
        Source xmlSource = new StreamSource(isToTransform);
        Source xsltSource = new StreamSource(xslt);

        // the factory pattern supports different XSLT processors
        TransformerFactory transFact =
                TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);

        trans.transform(xmlSource, new StreamResult(outputStream));
    }
}
