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

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Hits;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

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

    public Collection<OntologyTerm> search(String strQuery) throws IOException, ParseException {
        QueryParser queryParser = new QueryParser("identifier", new StandardAnalyzer());
        return search(queryParser.parse(strQuery), new Sort("identifier"));
    }

    public Collection<OntologyTerm> search(Query query, Sort sort) throws IOException {
        List<OntologyTerm> terms = new ArrayList<OntologyTerm>();

        IndexSearcher searcher = new IndexSearcher(indexDirectory);

        final Hits hits = searcher.search(query, sort);

        for (int i=0; i<hits.length(); i++) {
            Document document = hits.doc(i);

            OntologyTerm term = createOntologyTerm(document);
            terms.add(term);
        }

        searcher.close();

        return terms;
    }

    private OntologyTerm createOntologyTerm(Document document) {
        String identifier = document.getField("identifier").stringValue();
        String label = document.getField("label").stringValue();
        String databaseLabel = document.getField("databaseLabel").stringValue();

        return new OntologyTerm(identifier, label, databaseLabel);
    }
}
