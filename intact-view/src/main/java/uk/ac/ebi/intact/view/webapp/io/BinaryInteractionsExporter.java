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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.hupo.psi.calimocho.io.DocumentConverter;
import org.hupo.psi.calimocho.model.DocumentDefinition;
import org.hupo.psi.calimocho.tab.model.ColumnBasedDocumentDefinition;
import org.hupo.psi.calimocho.tab.util.MitabDocumentDefinitionFactory;
import org.hupo.psi.calimocho.xgmml.XGMMLDocumentDefinition;
import org.hupo.psi.mi.rdf.PsimiRdfConverter;
import org.hupo.psi.mi.rdf.RdfFormat;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.PsimiXmlForm;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.converter.ConverterContext;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.stylesheets.XslTransformException;
import psidev.psi.mi.xml.stylesheets.XslTransformerUtils;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.SolrSearchResult;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabWriter;
import uk.ac.ebi.intact.psimitab.IntactTab2Xml;
import uk.ac.ebi.intact.view.webapp.IntactViewException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Exports to MITAB
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryInteractionsExporter {

    private static final Log log = LogFactory.getLog( BinaryInteractionsExporter.class );
    
    private SolrServer solrServer;
    public static final String XML_2_53 = "xml_2_53";
    private static final String XML_2_54 = "xml_2_54";
    private static final String MITAB = "mitab";
    private static final String MITAB_INTACT = "mitab_intact";
    private static final String XML_HTML = "xml_html";
    private static final String BIOPAX_L2 = "biopax_l2";
    private static final String BIOPAX_L3 = "biopax_l3";
    private static final String RDF_XML = "rdf_xml";
    private static final String RDF_XML_ABBREV = "rdf_xml_abbrev";
    private static final String RDF_N3 = "rdf_n3";
    private static final String RDF_N3_PP = "rdf_n3_pp";
    private static final String RDF_TRIPLE = "rdf_triple";
    private static final String RDF_TURTLE = "rdf_turtle";
    private static final String XGMML = "xgmml";

    public BinaryInteractionsExporter(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public void searchAndExport( OutputStream os, SolrQuery searchQuery, String format ) throws IOException {
        if ( MITAB.equals( format ) ) {
            exportToMiTab( os, searchQuery );
        } else if ( MITAB_INTACT.equals( format ) ) {
            exportToMiTabIntact( os, searchQuery );
        } else if ( XML_2_53.equals( format ) || XML_2_54.equals( format ) ) {
            exportToMiXml( os, searchQuery, format );
        } else if ( XML_HTML.equals( format ) ) {
            exportToMiXmlTransformed( os, searchQuery );
        } else if ( BIOPAX_L2.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.BIOPAX_L2);
        } else if (BIOPAX_L3.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.BIOPAX_L3);
        } else if (RDF_XML.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.RDF_XML);
        } else if (RDF_XML_ABBREV.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.RDF_XML_ABBREV);
        } else if (RDF_N3.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.N3);
        } else if (RDF_N3_PP.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.N3_PP);
        } else if (RDF_TRIPLE.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.N_TRIPLE);
        } else if (RDF_TURTLE.equals(format)) {
            exportToRdf(os, searchQuery, RdfFormat.TURTLE);
        } else if (XGMML.equals(format)) {
            exportToXGMML(os, searchQuery);
        } else {
            throw new IntactViewException( "Format is not correct: " + format + ". Possible values: mitab, mitab_intact." );
        }
       
    }


    public void exportToMiTab(OutputStream os, SolrQuery searchQuery) throws IOException {
         PsimiTabWriter writer = new PsimiTabWriter();
         Writer out = new OutputStreamWriter(os);
         writeMitab(out, writer, searchQuery);
    }

    private void exportToMiTabIntact(OutputStream os, SolrQuery searchQuery) throws IOException, IntactViewException {
         PsimiTabWriter writer = new IntactPsimiTabWriter();
         Writer out = new OutputStreamWriter(os);
         writeMitab(out, writer, searchQuery);
    }

    private void writeMitab(Writer out, PsimiTabWriter writer, SolrQuery query) throws IOException {
        Integer firstResult = 0;
        Integer maxResults = 50;

        boolean headerEnabled = true;

        Collection interactions;

        do {
            //SolrQuery query = new SolrQuery(searchQuery);
            //SolrQuery query = convertToSolrQuery( searchQuery );
            query.setStart(firstResult);
            query.setRows(maxResults);

            IntactSolrSearcher searcher = new IntactSolrSearcher(solrServer);
            SolrSearchResult result = searcher.search(query);

            interactions = result.getBinaryInteractionList();


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

    public void exportToMiXml( OutputStream os, SolrQuery searchQuery ) throws IOException {
        exportToMiXml( os, searchQuery, XML_2_54);
    }

    public void exportToMiXml(OutputStream os, SolrQuery solrQuery, String format) throws IOException {
        Writer out = new OutputStreamWriter(os, "UTF-8");

        final EntrySet entrySet = createEntrySet(solrQuery);

        PsimiXmlWriter writer = null;
        if ( XML_2_53.equals( format ) ) {
            writer = new PsimiXmlWriter( PsimiXmlVersion.VERSION_253 );
        } else if ( XML_2_54.equals( format ) ) {
            writer = new PsimiXmlWriter( PsimiXmlVersion.VERSION_254 );
        } else {
            writer = new PsimiXmlWriter( PsimiXmlVersion.VERSION_25_UNDEFINED );
        }

        try {
            writer.write(entrySet, out);
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
    }

    public void exportToRdf(OutputStream os, SolrQuery searchQuery, RdfFormat format) throws IOException {
        Writer out = new OutputStreamWriter(os, "UTF-8");

        EntrySet entrySet = createEntrySet(searchQuery);

        PsimiRdfConverter converter = new PsimiRdfConverter();
        converter.convert(entrySet, format, out);
    }

    private void exportToXGMML(OutputStream os, SolrQuery solrQuery) throws IOException {
        SolrSearchResult result = findResult(solrQuery);

        Collection<String> interactions = result.getLineList();
        
        ColumnBasedDocumentDefinition mitabDefinition = MitabDocumentDefinitionFactory.mitab25Intact();
        DocumentDefinition definition = new XGMMLDocumentDefinition("IntAct Export", "Results for query: "+solrQuery.getQuery(), "IntAct");
        
        DocumentConverter converter = new DocumentConverter( mitabDefinition, definition );
        
        InputStream is = new ByteArrayInputStream(StringUtils.join(interactions, System.getProperty("line.separator")).getBytes());
        converter.convert( is, os );
    }

    private EntrySet createEntrySet(SolrQuery solrQuery) {
        Collection<IntactBinaryInteraction> interactions = findBinaryInteractions(solrQuery);

        Tab2Xml tab2Xml = new IntactTab2Xml();

        final EntrySet entrySet;
        try {
            entrySet = tab2Xml.convert(new ArrayList<BinaryInteraction>(interactions));
        } catch (Exception e) {
            throw new IntactViewException("Problem converting interactions from MITAB to XML", e);
        }
        return entrySet;
    }

    private Collection<IntactBinaryInteraction> findBinaryInteractions(SolrQuery solrQuery) {
        SolrSearchResult result = findResult(solrQuery);
        return result.getBinaryInteractionList();
    }

    private SolrSearchResult findResult(SolrQuery solrQuery) {
        IntactSolrSearcher searcher = new IntactSolrSearcher(solrServer);

        SolrSearchResult result1 = searcher.search(solrQuery);

        if (result1.getTotalCount() > 5000) {
            throw new IntactViewException("Too many interactions to export to XML. Maximum is 5000");
        }

        //SolrSearchResult result = searcher.search(searchQuery, null, null);
        solrQuery.setRows(Integer.MAX_VALUE);
        return searcher.search(solrQuery);
    }


}
