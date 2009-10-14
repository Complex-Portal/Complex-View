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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.DocIdBitSet;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologiesIndexSearcher {

    private Directory indexDirectory;

    public OntologiesIndexSearcher(Directory indexDirectory) {
        this.indexDirectory = indexDirectory;
    }

    public OntologyTerm findById(String value) throws IOException, ParseException {
        if (value == null || value.trim().length() == 0) return null;

        if (!value.startsWith("\"")) {
            value = "\"" + value + "\"";
        }

        OntologyTerm term = null;

        Collection<OntologyTerm> terms = search(value);

        if (!terms.isEmpty()) {
            term = terms.iterator().next();
        }

        return term;
    }

    public List<OntologyTerm> search(String strQuery) throws IOException, ParseException {
        QueryParser queryParser = new QueryParser("identifier", new StandardAnalyzer(Version.LUCENE_CURRENT));
        return search(queryParser.parse(strQuery), new Sort(new SortField("count", SortField.INT, true)));
    }

    public List<OntologyTerm> search(Query query, Sort sort) throws IOException {
        List<OntologyTerm> terms = new ArrayList<OntologyTerm>();

        final IndexSearcher searcher = new IndexSearcher(indexDirectory, true);

        Filter filter = new Filter() {
            @Override
            public DocIdSet getDocIdSet(IndexReader indexReader) throws IOException {
                final DocIdBitSet bitSet = new DocIdBitSet(new BitSet());
                bitSet.getBitSet().flip(0, searcher.getIndexReader().numDocs());
                return bitSet;
            }
        };

        final TopDocs hits = searcher.search(query, filter, 30, sort);

        Formatter formatter = new SimpleHTMLFormatter();
        Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query) );
        highlighter.setTextFragmenter(new SimpleFragmenter(20));

        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document document = searcher.getIndexReader().document(scoreDoc.doc);

            OntologyTerm term = createOntologyTerm(document, highlighter);
            terms.add(term);
        }

        searcher.close();

        return terms;
    }

    private OntologyTerm createOntologyTerm(Document document, Highlighter highlighter) throws IOException {
        String identifier = document.getField("identifier").stringValue();
        String label = document.getField("label").stringValue();
        String databaseLabel = document.getField("databaseLabel").stringValue();

        int count = Integer.parseInt(document.getField("count").stringValue());

        label = highlightText("label", label, highlighter);

        return new OntologyTerm(identifier, label, databaseLabel, count);
    }

    private String highlightText(String fieldName, String text, Highlighter highlighter) throws IOException {
        TokenStream tokenStream = new StandardAnalyzer(Version.LUCENE_CURRENT).tokenStream(fieldName, new StringReader(text));

        try {
            return highlighter.getBestFragments(tokenStream, text, 5, "...");
        } catch (Throwable e) {
            throw new IOException( e.getMessage() );
        }
    }
}
