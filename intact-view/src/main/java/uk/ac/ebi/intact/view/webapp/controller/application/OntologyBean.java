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
package uk.ac.ebi.intact.view.webapp.controller.application;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;
import uk.ac.ebi.intact.view.webapp.util.OntologiesIndexWriter;
import uk.ac.ebi.intact.view.webapp.util.OntologyTerm;
import uk.ac.ebi.intact.view.webapp.util.OntologiesIndexSearcher;
import uk.ac.ebi.intact.binarysearch.webapp.generated.Index;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Backing bean for Ontology Search and Autocomplete feature
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public class OntologyBean implements Serializable {

    private static final Log log = LogFactory.getLog( OntologyBean.class );

    private Directory ontologyIndexDirectory;
    private OntologiesIndexSearcher ontologiesIndexSearcher;

    public OntologyBean() {
        this.ontologyIndexDirectory = new RAMDirectory();
        this.ontologiesIndexSearcher = new OntologiesIndexSearcher(ontologyIndexDirectory);
    }

    public void loadOntologies(SearchConfig config) throws IOException {
        if (log.isInfoEnabled()) log.info("Loading and indexing ontologies");

        final Index defaultIndex = WebappUtils.getDefaultIndex(config);
        final Directory defaultIndexDirectory = FSDirectory.getDirectory(defaultIndex.getLocation());

        OntologiesIndexWriter ontologiesIndexWriter = new OntologiesIndexWriter();
        ontologiesIndexWriter.createIndex(defaultIndexDirectory, ontologyIndexDirectory);

        if (log.isInfoEnabled()) {
            int count = countDocsInIndex(ontologyIndexDirectory);
            log.info("Ontologies index created, containing "+count+" documents");
        }

    }

    private int countDocsInIndex(Directory ontologyIndexDirectory) {
        int count = -1;

        try {
            IndexSearcher searcher = new IndexSearcher(ontologyIndexDirectory);
            count = searcher.getIndexReader().maxDoc();
            searcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;
    }

    public Collection<OntologyTerm> search(String strQuery) throws IOException, ParseException {
        return ontologiesIndexSearcher.search(strQuery);
    }

    public Collection<OntologyTerm> search(Query query, Sort sort) throws IOException {
        return ontologiesIndexSearcher.search(query, sort);
    }
}
