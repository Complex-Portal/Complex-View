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

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import uk.ac.ebi.intact.view.webapp.controller.SearchWebappException;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;
import uk.ac.ebi.intact.view.webapp.util.JsfUtils;
import uk.ac.ebi.intact.view.webapp.util.OntologiesIndexSearcher;
import uk.ac.ebi.intact.view.webapp.util.OntologiesIndexWriter;
import uk.ac.ebi.intact.view.webapp.util.OntologyTerm;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private ApplicationContext applicationContext;

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

    public void loadOntologies() throws IOException {
        if (intactViewConfiguration.getInteractionSolrServer() == null) {
           if (log.isErrorEnabled()) log.error("Cannot load ontologies as the Solr server is not configured");
            return;
        }
        if (log.isInfoEnabled()) log.info("Loading and indexing ontologies");

        OntologiesIndexWriter ontologiesIndexWriter = new OntologiesIndexWriter();
        ontologiesIndexWriter.createIndex(intactViewConfiguration.getInteractionSolrServer(), ontologyIndexDirectory);

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
        List<OntologyTerm> otherResult = Lists.newArrayList();

        if (result.size() > intactViewConfiguration.getMaxOntologySuggestions()) {
            final int furtherTermCount = result.size() - intactViewConfiguration.getMaxOntologySuggestions();
            otherResult = result.subList(intactViewConfiguration.getMaxOntologySuggestions()-1,result.size()  );
            result = result.subList(0, intactViewConfiguration.getMaxOntologySuggestions()-1);

            int otherCount =0;
            for ( OntologyTerm ontologyTerm : otherResult ) {
               otherCount = otherCount + ontologyTerm.getCount();
            }
            //result.add(new OntologyTerm("*", "There are "+ furtherTermCount +" more term"+ (furtherTermCount > 1 ? "s" : "") +"...", "na"));
            result.add(new OntologyTerm("*", "There are "+ furtherTermCount +" more term"+ (furtherTermCount > 1 ? "s" : "") +"...", "na", otherCount ));
        }

        //clear the term map otherwise it keeps growing
        //this termmap is used for display query of Go term with id:name, Ref: getDisplayQuery() in UserQuery.java
        UserQuery userQuery = (UserQuery) applicationContext.getBean("userQuery");
        userQuery.getTermMap().clear();

        for ( OntologyTerm ontologyTerm : result ) {
           userQuery.getTermMap().put( ontologyTerm.getIdentifier(), ontologyTerm.getLabel() );
        }
        final ValueExpression ve = facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{autocompleteResult}", Collection.class);
        ve.setValue(facesContext.getELContext(), result);
    }

    public static String prepareOntologyQueryForLucene( String strFieldValue, boolean addWildcard ) {

        if ( strFieldValue == null ) {
            return "*";
        }

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

    public OntologiesIndexSearcher getOntologiesIndexSearcher() {
        return ontologiesIndexSearcher;
    }

}
