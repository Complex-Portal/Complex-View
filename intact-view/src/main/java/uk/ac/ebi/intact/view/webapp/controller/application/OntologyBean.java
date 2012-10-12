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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.InteractionOntologyLuceneSearcher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.InteractionOntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.InteractionOntologyTermResults;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
/**
 * Backing bean for Ontology Search and Autocomplete feature
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public class OntologyBean extends BaseController implements InitializingBean {

    private static final Log log = LogFactory.getLog( OntologyBean.class );

    private File ontologyIndexDirectory;

    private InteractionOntologyLuceneSearcher ontologiesIndexSearcher;

    private int maxOntologySuggestion;

    @Autowired
    private IntactViewConfiguration viewConfiguration;

    public OntologyBean() {
    }

    public InteractionOntologyTerm findByIdentifier(String id) {
        if (ontologiesIndexSearcher == null){
           return new InteractionOntologyTerm("", id);
        }

        final InteractionOntologyTerm term;
        try {
            term = ontologiesIndexSearcher.findById(id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Problem loading term: "+id, e);
        }
        return term;
    }
    public InteractionOntologyTerm findByName(String id) {
        if (ontologiesIndexSearcher == null){
            return new InteractionOntologyTerm(id, "");
        }

        final InteractionOntologyTerm term;
        try {
            term = ontologiesIndexSearcher.findByName(id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Problem loading term: "+id, e);
        }
        return term;
    }


    public List<InteractionOntologyTerm> search(String strQuery) throws IOException, ParseException {
        return ontologiesIndexSearcher.search(strQuery);
    }

    public List<InteractionOntologyTerm> search(Query query, Sort sort) throws IOException {
        return ontologiesIndexSearcher.search(query, sort);
    }

    //for Autocomplete box

    public List<InteractionOntologyTerm> fillAutocomplete( String query ) {
        if (ontologiesIndexSearcher == null){
           return Collections.EMPTY_LIST;
        }
        String formattedQuery = prepareOntologyQueryForLucene( query, true );

        if ( log.isDebugEnabled() ) {
            log.debug( "Original Query  " + query );
            log.debug( "Query formatted for Lucene  " + formattedQuery );
        }

        try {
            List<InteractionOntologyTerm> result = search( formattedQuery );
            List<InteractionOntologyTerm> otherResult;

            if (result.size() > maxOntologySuggestion) {
                final int furtherTermCount = result.size() - maxOntologySuggestion;
                otherResult = result.subList(maxOntologySuggestion-1,result.size()  );
                result = result.subList(0, maxOntologySuggestion-1);

                long otherCount =0;
                for ( InteractionOntologyTerm ontologyTerm : otherResult ) {
                    otherCount = otherCount + ontologyTerm.getResults().getCount();
                }
                InteractionOntologyTerm term = new InteractionOntologyTerm("There are "+ furtherTermCount +" more term"+ (furtherTermCount > 1 ? "s" : "") +"...", "");
                term.setResults(new InteractionOntologyTermResults("na", "na", otherCount));
                result.add(term);
            }

            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Problem populating ontology terms with query: "+query, e);
        }

    }

    public static String prepareOntologyQueryForLucene( String strFieldValue, boolean addWildcard ) {
        if ( strFieldValue == null ) {
            return "*";
        }

        strFieldValue = strFieldValue.trim();

        //some terms may contains hyphen eg: S-adenosyl-L-methionine,
        // and searching for adenosyl-* will not return any result as - is a Lucene Keyword
        if ( strFieldValue.contains( "-" ) ) {
            strFieldValue = strFieldValue.replaceAll( "-", "\\\\-" );
            addWildcard = false;
        }
        if (addWildcard) {
            if ( !strFieldValue.endsWith( "*" ) ) {
                strFieldValue = strFieldValue + "*";
            }
        }

        if ( strFieldValue.contains( ":" ) ) {
            if ( !strFieldValue.startsWith( "\"" ) && !strFieldValue.endsWith( "\"" ) ) {
                strFieldValue = "\"" + strFieldValue + "\"";
            }
        }

        strFieldValue = "identifier:" + strFieldValue + " label:" + strFieldValue;

        return strFieldValue;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (viewConfiguration.getOntologyLuceneDirectory() == null) {
            if (log.isErrorEnabled()) log.error("Cannot load ontologies as the ontology lucene directory is not configured");
            return;
        }
        if (log.isInfoEnabled()) log.info("Loading and indexing ontologies");

        this.ontologyIndexDirectory = new File(viewConfiguration.getOntologyLuceneDirectory());
        this.ontologiesIndexSearcher = new InteractionOntologyLuceneSearcher(this.ontologyIndexDirectory);

        this.maxOntologySuggestion = viewConfiguration.getMaxOntologySuggestions();
    }
}
