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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.hupo.psi.calimocho.io.IllegalRowException;
import org.hupo.psi.calimocho.tab.util.MitabDocumentDefinitionFactory;
import org.hupo.psi.calimocho.xgmml.XgmmlStreamingGrapBuilder;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrException;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrServer;
import org.hupo.psi.mi.rdf.PsimiRdfConverter;
import org.hupo.psi.mi.rdf.RdfFormat;
import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.tab2xml.XmlConversionException;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.stylesheets.XslTransformException;
import psidev.psi.mi.xml.stylesheets.XslTransformerUtils;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearchResult;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.view.webapp.IntactViewException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.*;

/**
 * Exports to MITAB
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryInteractionsExporter {

    private static final Log log = LogFactory.getLog( BinaryInteractionsExporter.class );

    private IntactSolrSearcher solrSearcher;
    public static final String XML_2_53 = "xml_2_53";
    public static final String XML_2_54 = "xml_2_54";
    public static final String MITAB_25 = "mitab_25";
    public static final String MITAB_26 = "mitab_26";
    public static final String MITAB_27 = "mitab_27";
    public static final String XML_HTML = "xml_html";
    public static final String BIOPAX_L2 = "biopax_l2";
    public static final String BIOPAX_L3 = "biopax_l3";
    public static final String RDF_XML = "rdf_xml";
    public static final String RDF_XML_ABBREV = "rdf_xml_abbrev";
    public static final String RDF_N3 = "rdf_n3";
    public static final String RDF_N3_PP = "rdf_n3_pp";
    public static final String RDF_TRIPLE = "rdf_triple";
    public static final String RDF_TURTLE = "rdf_turtle";
    public static final String XGMML = "xgmml";

    public BinaryInteractionsExporter(SolrServer solrServer) {
        this.solrSearcher = new IntactSolrSearcher(solrServer);
    }

    public String searchAndExport( OutputStream os, SolrQuery searchQuery, String format) throws IOException, IllegalRowException {
        if ( MITAB_25.equals( format ) || MITAB_26.equals( format ) || MITAB_27.equals( format ) ) {
            exportToMiTab( os, searchQuery, format );
            return "text/plain";
        } else if ( XML_2_53.equals( format ) || XML_2_54.equals( format ) ) {
            exportToMiXml(os, searchQuery, format);
            return "application/xml";
        } else if ( XML_HTML.equals( format ) ) {
            exportToMiXmlTransformed(os, searchQuery);
            return "text/html";
        } else if ( BIOPAX_L2.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.BIOPAX_L2);
            return "application/xml";
        } else if (BIOPAX_L3.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.BIOPAX_L3);
            return "application/xml";
        } else if (RDF_XML.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.RDF_XML);
            return "application/rdf+xml";
        } else if (RDF_XML_ABBREV.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.RDF_XML_ABBREV);
            return "application/rdf+xml";
        } else if (RDF_N3.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.N3);
            return "text/plain";
        } else if (RDF_N3_PP.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.N3_PP);
            return "text/plain";
        } else if (RDF_TRIPLE.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.N_TRIPLE);
            return "text/plain";
        } else if (RDF_TURTLE.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.TURTLE);
            return "text/plain";
        } else if (XGMML.equals(format)) {
            exportToXGMML(os, searchQuery);
            return "application/xgmml";
        } else {
            throw new IntactViewException( "Format is not correct: " + format + ". Possible values: mitab, mitab_intact." );
        }

    }

    public void exportToMiTab(OutputStream os, SolrQuery searchQuery, String format) throws IOException {
        PsimiTabVersion version = PsimiTabVersion.v2_7;
        String psicquicFormat = PsicquicSolrServer.RETURN_TYPE_MITAB27;
        if (MITAB_25.equals(format)){
           version = PsimiTabVersion.v2_5;
            psicquicFormat = PsicquicSolrServer.RETURN_TYPE_MITAB25;
        }
        else if (MITAB_26.equals(format)){
            version = PsimiTabVersion.v2_6;
            psicquicFormat = PsicquicSolrServer.RETURN_TYPE_MITAB26;
        }

        PsimiTabWriter writer = new PsimiTabWriter(version);
        Writer out = new BufferedWriter(new OutputStreamWriter(os));
        try{
            writeMitab(out, writer, searchQuery, psicquicFormat);
        }
        finally {
            // close writer
            out.close();
        }
    }

    private void writeMitab(Writer out, PsimiTabWriter writer, SolrQuery query, String psicquicFormat) throws IOException {
        Integer firstResult = 0;
        Integer maxResults = 500;
        int totalResults = 0;

        // write header first
        writer.writeMitabHeader(out);

        do {
            SolrQuery queryCopy = query.getCopy();

            queryCopy.setStart(firstResult);
            queryCopy.setRows(maxResults);

            IntactSolrSearchResult result = null;
            try {
                result = solrSearcher.search(queryCopy, psicquicFormat);

                InputStream mitabStream = result.getMitab();
                if (mitabStream != null){
                    IOUtils.copy(mitabStream, out);
                }

                firstResult = firstResult + maxResults;
                totalResults = Long.valueOf(result.getNumberResults()).intValue();
                out.flush();

            } catch (PsicquicSolrException e) {
                throw new IntactViewException("Impossible to find the results of this query " + query.getQuery(), e);
            } catch (SolrServerException e) {
                throw new IntactViewException("Impossible to find the results of this query " + query.getQuery(), e);
            }

        } while (firstResult < totalResults);
    }
    private void writeXGMML(OutputStream out, SolrQuery query) throws IOException {
        Integer firstResult = 0;
        Integer maxResults = 500;
        int totalResults = 0;

        XgmmlStreamingGrapBuilder graphBuilder = null;
        try {
            graphBuilder = new XgmmlStreamingGrapBuilder("IntAct Export "+System.currentTimeMillis(), "Results for query: "+query.getQuery(), "IntAct");

            boolean started = false;

            do {
                SolrQuery queryCopy = query.getCopy();
                queryCopy.setStart(firstResult);
                queryCopy.setRows(maxResults);

                IntactSolrSearchResult result = null;
                try {
                    result = solrSearcher.search(queryCopy);

                    if (!started){
                        started = true;
                        totalResults = Long.valueOf(result.getNumberResults()).intValue();

                        graphBuilder.open(out, totalResults);

                    }

                    InputStream is = result.getMitab();

                    try {
                        graphBuilder.writeNodesAndEdgesFromMitab(is, MitabDocumentDefinitionFactory.mitab27());
                    }finally {
                        is.close();
                    }

                    firstResult = firstResult + maxResults;

                    out.flush();

                } catch (PsicquicSolrException e) {
                    throw new IntactViewException("Impossible to find the results of this query " + query.getQuery(), e);
                } catch (SolrServerException e) {
                    throw new IntactViewException("Impossible to find the results of this query " + query.getQuery(), e);
                }

            } while (firstResult < totalResults);
        } catch (JAXBException e) {
            new IntactViewException("Problem exporting interactions", e);
        } catch (XMLStreamException e) {
            new IntactViewException("Problem exporting interactions", e);
        }
        finally {
            if (graphBuilder != null){
                try {
                    graphBuilder.close();
                } catch (XMLStreamException e) {
                    new IntactViewException("Problem exporting interactions", e);
                }
            }
        }
    }


    public void exportToMiXml( OutputStream os, SolrQuery searchQuery ) throws IOException {
        exportToMiXml( os, searchQuery, XML_2_54);
    }

    public void exportToMiXml(OutputStream os, SolrQuery solrQuery, String format) throws IOException {

        final EntrySet entrySet = createEntrySet(solrQuery.getCopy());

        PsimiXmlWriter writer = null;
        if ( XML_2_53.equals( format ) ) {
            writer = new PsimiXmlWriter( PsimiXmlVersion.VERSION_253 );
        } else if ( XML_2_54.equals( format ) ) {
            writer = new PsimiXmlWriter( PsimiXmlVersion.VERSION_254 );
        } else {
            writer = new PsimiXmlWriter( PsimiXmlVersion.VERSION_25_UNDEFINED );
        }

        try {
            Writer out = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            try{
                writer.write(entrySet, out);
            }
            finally {

                // close writer
                out.close();
            }
        } catch (Exception e) {
            throw new IntactViewException("Problem writing XML (format "+format+") for query: "+solrQuery, e);
        }
    }

    public void exportToMiXmlTransformed(OutputStream os, SolrQuery searchQuery) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        exportToMiXml(baos, searchQuery);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toString().getBytes());

        try {
            XslTransformerUtils.viewPsiMi25( bais, os );

            //transform(os, bais, BinaryInteractionsExporter.class.getResourceAsStream("/META-INF/MIF254_view.xsl"));
        }  catch ( XslTransformException e ) {
            throw new IntactViewException("Problem transforming XML to HTML(XslTransformException)", e);
        }
        finally {
            // close baos and bais
            baos.close();
            bais.close();
        }
    }

    public void exportToRdf(OutputStream os, SolrQuery searchQuery, RdfFormat format) throws IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

        try{
            EntrySet entrySet = createEntrySet(searchQuery);

            if (entrySet != null && !entrySet.getEntries().isEmpty()){
                PsimiRdfConverter converter = new PsimiRdfConverter();
                converter.convert(entrySet, format, out);
            }
        }
        finally {
            // close writer
            out.close();
        }
    }

    private void exportToXGMML(OutputStream os, SolrQuery solrQuery) throws IOException, IllegalRowException {

        writeXGMML(os, solrQuery);
    }

    private EntrySet createEntrySet(SolrQuery solrQuery) {
        IntactSolrSearchResult result1 = null;
        try {
            result1 = solrSearcher.search(solrQuery, PsicquicSolrServer.RETURN_TYPE_XML25);
            if (result1.getNumberResults() > 5000) {
                throw new IntactViewException("Too many interactions to export to XML. Maximum is 5000");
            }

            EntrySet entrySet = result1.createEntrySet();

            return entrySet;

        } catch (PsicquicSolrException e) {
            throw new IntactViewException("Impossible to find the results of this query " + solrQuery.getQuery(), e);
        } catch (SolrServerException e) {
            throw new IntactViewException("Impossible to find the results of this query " + solrQuery.getQuery(), e);
        } catch (IOException e) {
            throw new IntactViewException("Impossible to convert the xml results of this query " + solrQuery.getQuery(), e);
        } catch (IllegalAccessException e) {
            throw new IntactViewException("Impossible to convert the xml results of this query " + solrQuery.getQuery(), e);
        } catch (XmlConversionException e) {
            throw new IntactViewException("Impossible to convert the xml results of this query " + solrQuery.getQuery(), e);
        } catch (PsimiTabException e) {
            throw new IntactViewException("Impossible to convert the xml results of this query " + solrQuery.getQuery(), e);
        }
    }

}
