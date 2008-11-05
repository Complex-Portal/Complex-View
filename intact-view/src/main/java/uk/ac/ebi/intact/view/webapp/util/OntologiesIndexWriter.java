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
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import psidev.psi.mi.tab.model.builder.CrossReferenceFieldBuilder;
import psidev.psi.mi.tab.model.builder.FieldBuilder;
import psidev.psi.mi.tab.model.builder.ParseUtils;

import java.io.IOException;
import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologiesIndexWriter {

private static final Log log = LogFactory.getLog( OntologiesIndexWriter.class );

    public OntologiesIndexWriter() {
    }

    public void createIndex(Directory binaryInteractionIndex, Directory newDirectory)
            throws IOException {

        final IndexSearcher indexSearcher = new IndexSearcher(binaryInteractionIndex);
        final IndexReader indexReader = indexSearcher.getIndexReader();

        final IndexWriter termIndexWriter = new IndexWriter(newDirectory, new StandardAnalyzer(), true);

        // create a field selector just to get the fields we need
        FieldSelector fieldSelector = new MapFieldSelector(new String[] {"detmethod", "type", "propertiesA", "propertiesB"});

        OntologyTermBuilder termBuilder = new OntologyTermBuilder();

        Set<String> processed = new HashSet<String>();
        
        // iterate through all the documents in the binary interaction index
        for (int i=0; i<indexReader.maxDoc(); i++) {
            if (indexReader.isDeleted(i)) {
                continue;
            }

            Document document = indexReader.document(i, fieldSelector);

            Collection<OntologyTerm> terms = termBuilder.createOntologyTerms(document);

            // write the ontology terms in the new index
            for (OntologyTerm term : terms) {
            	if (processed.contains(term.getIdentifier())) {
            		continue;
            	}
            	
                Document termDocument = createDocument(term);
                termIndexWriter.addDocument(termDocument);
                
                processed.add(term.getIdentifier());
            }
        }

        indexReader.close();
        indexSearcher.close();

        termIndexWriter.optimize();
        termIndexWriter.close();

    }

    private Document createDocument(OntologyTerm term) {
    	Document document = new Document();

    	document.add(new Field("identifier", term.getIdentifier(), Store.YES, Index.UN_TOKENIZED));

        if (term.getLabel() != null) {
    	    document.add(new Field("label", term.getLabel(), Store.YES, Index.TOKENIZED));
    	    document.add(new Field("label_sorted", term.getLabel(), Store.NO, Index.UN_TOKENIZED));
        }
        
    	document.add(new Field("databaseLabel", term.getDatabaseLabel(), Store.YES, Index.TOKENIZED));
    	document.add(new Field("databaseLabel_sorted", term.getDatabaseLabel(), Store.NO, Index.UN_TOKENIZED));

        return document;
    }

    private class OntologyTermBuilder {

        private static final String GO_DATABASE_TYPE = "go";
        private static final String CHEBI_DATABASE_TYPE = "chebi";
        private static final String INTERPRO_DATABASE_TYPE = "interpro";


        public OntologyTermBuilder() {
        }

        public Collection<OntologyTerm> createOntologyTerms(Document luceneDocument) {
            List<OntologyTerm> terms = new ArrayList<OntologyTerm>();

            final String detMethodFieldName = "detmethod";
            final String interactionTypeFieldName = "type";
            final String propertiesAFieldName = "propertiesA";
            final String propertiesBFieldName = "propertiesB";

            // this field contains the parents too
            Field detMethodField = luceneDocument.getField(detMethodFieldName);
            // this field contains the parents too
            Field interactionTypeField = luceneDocument.getField(interactionTypeFieldName);
            // we will just use GO terms from the properties
            Field propertiesAField = luceneDocument.getField(propertiesAFieldName);
            Field propertiesBField = luceneDocument.getField(propertiesBFieldName);

            Collection<OntologyTerm> detMethodTerms = createOntologyTerms(detMethodField, null);
            Collection<OntologyTerm> intTypeTerms = createOntologyTerms(interactionTypeField, null);

            Collection<OntologyTerm> goTerms = new HashSet<OntologyTerm>();
            goTerms.addAll(createOntologyTerms(propertiesAField, GO_DATABASE_TYPE));
            goTerms.addAll(createOntologyTerms(propertiesBField, GO_DATABASE_TYPE));

            Collection<OntologyTerm> chebiTerms = new HashSet<OntologyTerm>();
            chebiTerms.addAll(createOntologyTerms(propertiesAField, CHEBI_DATABASE_TYPE));
            chebiTerms.addAll(createOntologyTerms(propertiesBField, CHEBI_DATABASE_TYPE));

            Collection<OntologyTerm> interproTerms = new HashSet<OntologyTerm>();
            interproTerms.addAll(createOntologyTerms(propertiesAField, INTERPRO_DATABASE_TYPE));
            interproTerms.addAll(createOntologyTerms(propertiesBField, INTERPRO_DATABASE_TYPE));

            terms.addAll(detMethodTerms);
            terms.addAll(intTypeTerms);
            terms.addAll(goTerms);
            terms.addAll( chebiTerms );
            terms.addAll( interproTerms );

            return terms;
        }


        private Collection<OntologyTerm> createOntologyTerms(Field lucenePropertiesField, String type) {
            List<OntologyTerm> terms = new ArrayList<OntologyTerm>();

            if (lucenePropertiesField == null) {
                return terms;
            }

            String lucenePropertiesValue = lucenePropertiesField.stringValue();

            String[] strFields = ParseUtils.quoteAwareSplit(lucenePropertiesValue, new char[]{'|'}, false);

            FieldBuilder fieldBuilder = new CrossReferenceFieldBuilder();

            for (String strField : strFields) {
                psidev.psi.mi.tab.model.builder.Field field = fieldBuilder.createField(strField);

                if (field == null) {
                    continue;
                }

                OntologyTerm term = null;

                if (type != null) {
                    if (type.equals(field.getType())) {
                       term = createTermFromField(field);
                    }
                } else {
                    term = createTermFromField(field);
                }

                if (term != null) {
                    terms.add(term);
                }
            }

            return terms;
        }

        private OntologyTerm createTermFromField(psidev.psi.mi.tab.model.builder.Field field) {
            return new OntologyTerm(field.getValue(), field.getDescription(), field.getType());
        }
    }

}

