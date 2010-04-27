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
package uk.ac.ebi.intact.view.webapp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;
import psidev.psi.mi.tab.model.builder.CrossReferenceFieldBuilder;
import psidev.psi.mi.tab.model.builder.FieldBuilder;
import uk.ac.ebi.intact.view.webapp.IntactViewException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Writes the ontologies using data from the used terms (go, interpro, chebi,
 * detection method and interaction type) to a lucene index.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologiesIndexWriter {

    private static final Log log = LogFactory.getLog( OntologiesIndexWriter.class );

    public OntologiesIndexWriter() {
    }

    public void createIndex(SolrServer solrServer, Directory newDirectory)
            throws IOException {

        Collection<FieldCount> detMethodFields = null;
        Collection<FieldCount> typeFields = null;
        Collection<FieldCount> goFields = null;
        Collection<FieldCount> inteproFields = null;
        Collection<FieldCount> chebiFields = null;
        Collection<FieldCount> taxonomyFields = null;

        try {
            detMethodFields = loadAllTermsForField(solrServer, "detmethod_expanded_ms", "detmethod");
            typeFields = loadAllTermsForField(solrServer, "type_expanded_ms", "type");
            goFields = loadAllTermsForField(solrServer, "go_expanded_ms", "go");
            inteproFields = loadAllTermsForField(solrServer, "interpro_expanded_ms", "interpro");
            chebiFields = loadAllTermsForField(solrServer, "chebi_expanded_ms", "chebi");
            taxonomyFields = loadAllTermsForField(solrServer, "taxid_expanded_ms", "species");
        } catch (SolrServerException e) {
            throw new IntactViewException("Problem loading terms from SolrServer: "+solrServer, e);
        }

        final IndexWriter termIndexWriter = new IndexWriter(newDirectory, new StandardAnalyzer(Version.LUCENE_CURRENT), true);

        addDocsToIndex(termIndexWriter, createDocuments(detMethodFields));
        addDocsToIndex(termIndexWriter, createDocuments(typeFields));
        addDocsToIndex(termIndexWriter, createDocuments(goFields));
        addDocsToIndex(termIndexWriter, createDocuments(inteproFields));
        addDocsToIndex(termIndexWriter, createDocuments(chebiFields));
        addDocsToIndex(termIndexWriter, createDocuments(taxonomyFields));

        termIndexWriter.optimize();
        termIndexWriter.close();

    }

    private void addDocsToIndex(IndexWriter termIndexWriter, Collection<Document> detMethodDocs) throws IOException {
        for (Document doc : detMethodDocs) {
            termIndexWriter.addDocument(doc);
        }
    }

    private Collection<Document> createDocuments(Collection<FieldCount> fieldCounts) {
        List<Document> docs = new ArrayList<Document>(fieldCounts.size());

        for (FieldCount fieldCount : fieldCounts) {
            final psidev.psi.mi.tab.model.builder.Field field = fieldCount.getField();
            if( field.getType() == null || field.getValue() == null ) {
                log.warn( "Skipping invalid Field: [Type:" + field.getType() + " | Value:" + field.getValue() + " | Description:" + field.getDescription() + "]" );
                continue;
            }
            if ("MI:0000".equals(field.getValue())) {
                log.debug("Skipping root MI:0000");
                continue;
            }
            Document doc = createDocument(fieldCount);
            docs.add(doc);
        }

        return docs;
    }

    private Document createDocument(FieldCount fieldCount) {
        Document document = new Document();

    	document.add(new Field("identifier", fieldCount.getField().getValue(), Store.YES, Index.NOT_ANALYZED));

        if (fieldCount.getField().getDescription() != null) {
    	    document.add(new Field("label", fieldCount.getField().getDescription(), Store.YES, Index.ANALYZED));
    	    document.add(new Field("label_sorted", fieldCount.getField().getDescription(), Store.NO, Index.NOT_ANALYZED));
        }

        document.add(new Field("databaseLabel", fieldCount.getField().getType(), Store.YES, Index.ANALYZED));
    	document.add(new Field("databaseLabel_sorted", fieldCount.getField().getType(), Store.NO, Index.NOT_ANALYZED));

        document.add(new Field("count", String.valueOf(fieldCount.getCount()), Store.YES, Index.NOT_ANALYZED));

        document.add(new Field("fieldName", fieldCount.getSearchFieldName(), Store.YES, Index.NOT_ANALYZED));

        return document;
    }

    private List<FieldCount> loadAllTermsForField(SolrServer solrServer, String fieldName, String searchFieldName) throws SolrServerException {
        SolrQuery query = new SolrQuery("*:*")
                .setRows(0)
                .setFields(fieldName)
                .setFacet(true)
                .setFacetLimit(-1)
                .addFacetField(fieldName)
                .setFacetSort(FacetParams.FACET_SORT_COUNT);

        QueryResponse queryResponse = solrServer.query(query);

        FacetField facetField = queryResponse.getFacetField(fieldName);

        List<FieldCount> fields = new ArrayList<FieldCount>(Long.valueOf(queryResponse.getResults().getNumFound()).intValue());

        FieldBuilder fieldBuilder = new CrossReferenceFieldBuilder();

        if (facetField != null && facetField.getValues() != null) {
            for (FacetField.Count c : facetField.getValues()) {
                psidev.psi.mi.tab.model.builder.Field field = fieldBuilder.createField(c.getName());
                FieldCount fc = new FieldCount(field, c.getCount(), searchFieldName);
                fields.add(fc);
            }
        }

        if (log.isDebugEnabled()) log.debug("Terms found for fieldName '"+fieldName+"': "+fields.size()+" / query: "+query);

        return fields;
    }

    private static class FieldCount {

        private psidev.psi.mi.tab.model.builder.Field field;
        private String searchFieldName;
        private long count;

        private FieldCount(psidev.psi.mi.tab.model.builder.Field field, long count, String searchFieldName) {
            this.field = field;
            this.count = count;
            this.searchFieldName = searchFieldName;
        }

        public psidev.psi.mi.tab.model.builder.Field getField() {
            return field;
        }

        public long getCount() {
            return count;
        }

        public String getSearchFieldName() {
            return searchFieldName;
        }
    }
}