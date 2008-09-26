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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.binarysearch.webapp.generated.Index;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.view.webapp.util.*;
import uk.ac.ebi.intact.view.webapp.controller.SearchWebappException;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
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

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    public OntologyBean() {
        String tempDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tempDir, "intact-view-"+System.currentTimeMillis());

        try {
            FileUtils.forceDeleteOnExit(dir);
            this.ontologyIndexDirectory = FSDirectory.getDirectory(dir);
        } catch (IOException e) {
            throw new SearchWebappException("Problem creating ontology lucene directory", e);
        }
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

    public List<OntologyTerm> search(String strQuery) throws IOException, ParseException {
        return ontologiesIndexSearcher.search(strQuery);
    }

    public List<OntologyTerm> search(Query query, Sort sort) throws IOException {
        return ontologiesIndexSearcher.search(query, sort);
    }

//for Autocomplete box 


    @SuppressWarnings( "unchecked" )
    public void fillAutocomplete( ActionEvent event ) throws IOException, ParseException {

        final FacesContext facesContext = FacesContext.getCurrentInstance();

        final Map parameters = facesContext.getExternalContext().getRequestParameterMap();
        final Object fieldValue = parameters.get(JsfUtils.getParameterValue( "searchFieldRequestParamName", event ) );

        String strFieldValue = fieldValue == null || fieldValue.toString().trim().length() == 0 ? "" : fieldValue.toString().trim().toLowerCase();

        String formattedQuery = prepareOntologyQueryForLucene( strFieldValue, true );

        if ( log.isDebugEnabled() ) {
            log.debug( "Original Query  " + strFieldValue );
            log.debug( "Query formatted for Lucene  " + formattedQuery );
        }

        List<OntologyTerm> result = search( formattedQuery );

        if (result.size() > intactViewConfiguration.getMaxOntologySuggestions()) {
            result = result.subList(0, intactViewConfiguration.getMaxOntologySuggestions()-1);
            result.add(new OntologyTerm("*", "There are more terms...", "na"));
        }

        final ValueExpression ve = facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{autocompleteResult}", Collection.class);
        ve.setValue(facesContext.getELContext(), result);
    }

    public static String prepareOntologyQueryForLucene( String strFieldValue, boolean addWildcard ) {

        if ( strFieldValue == null ) {
            return "*";
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

    public OntologiesIndexSearcher getOntologiesIndexSearcher() {
        return ontologiesIndexSearcher;
    }

}
