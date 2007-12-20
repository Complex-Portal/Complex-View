/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.highlightment.source.edge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.Author;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.service.graph.Edge;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class PMIDHighlightmentSource extends EdgeHighlightmentSource {


    private static final Log logger = LogFactory.getLog( PMIDHighlightmentSource.class );

    /**
     * The key for this source 'pmid'
     */
    public static final String SOURCE_KEY;

    public static final String SOURCE_CLASS;

    private static final String applicationPath;

    static {
        // get in the Highlightment properties file where is interpro
        Properties props = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null == props ) {
            String msg = "Unable to find the PMID hostname. "
                         + "The properties file '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' couldn't be loaded.";
            logger.error( msg );
            throw new IntactException( msg );
        }
        SOURCE_KEY = props.getProperty( "highlightment.source.edge.PMID.label" );

        if ( null == SOURCE_KEY ) {
            String msg = "Unable to find the PMID Label. "
                         + "Check the 'highlightment.source.edge.PMID.label' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        applicationPath = props.getProperty( "highlightment.source.edge.PMID.applicationPath" );

        if ( null == applicationPath ) {
            String msg = "Unable to find the interpro hostname. "
                         + "Check the 'highlightment.source.edge.PMID.applicationPath' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        SOURCE_CLASS = props.getProperty( "highlightment.source.edge.PMID.class" );

        if ( null == SOURCE_CLASS ) {
            String msg = "Unable to find the PMID Class. "
                         + "Check the 'highlightment.source.edge.PMID.class' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }
    }

    public String getHtmlCodeOption( HttpSession aSession ) {

        return "a Html Code";
    }

    public Collection<Edge> interactionToHightlight( HttpSession aSession, Network aGraph ) {

        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
        Collection children = user.getKeys();
        String selectedTerm = user.getSelectedKey();

        logger.debug( "getKeys=" + children + " | selectedTerm=" + selectedTerm );
        if ( children.remove( selectedTerm ) ) {
            if ( logger.isDebugEnabled() ) logger.debug( selectedTerm + " removed from children collection" );
        }

        if ( aGraph.isEdgeHighlightMapEmpty() ) {
            aGraph.initHighlightMap();
        }

        return edgeToHighlightSourceMap( aGraph, selectedTerm );
    }

    /**
     * Returns a collection of proteins to be highlighted in the graph.
     * <p/>
     * Method is called when the graph was built by the mine database table.
     *
     * @param aGraph       the graph
     * @param selectedTerm the selected PMID Term
     * @return
     */
    private Collection<Edge> edgeToHighlightSourceMap( Network aGraph, String selectedTerm ) {

        Collection<Edge> edgeList = new ArrayList( 20 ); // should be enough for 90% cases

        // retrieve the set of proteins to highlight for the source key (e.g. PMID) and the selected PMID Term
        Set<Edge> edgesToHighlight = aGraph.getEdgesForHighlight( SOURCE_KEY, selectedTerm );

        // if we found any proteins we add all of them to the collection
        if ( edgesToHighlight != null ) {
            edgeList.addAll( edgesToHighlight );
        }

        return edgeList;
    }

    public List<SourceBean> getSourceUrls( Network network, Collection<String> selectedTerms, String applicationPath, IntactUserI user ) {

        List<SourceBean> urls = new ArrayList();
        Map highlightPMIDMap = ( Map ) network.getEdgeHighlightMap().get( SOURCE_KEY );

        if ( highlightPMIDMap != null && !highlightPMIDMap.isEmpty() ) {

            Set<String> keySet = highlightPMIDMap.keySet();

            if ( keySet != null && !keySet.isEmpty() ) {

                for ( String termId : keySet ) {

                    Author author = network.getAuthorByPMID( termId );
                    String termDescription = null;
                    if ( author != null ) {
                        termDescription = author.getName();
                    }
                    String termType = SOURCE_KEY;

                    int termCount = network.getDatabaseTermCount( termType, termId );

                    // to summarize
                    if ( logger.isDebugEnabled() ) {
                        logger.debug( "TermType=" + termType + " | " +
                                      "TermId=" + termId + " | " +
                                      "TermDescription=" + termDescription + " | " +
                                      "TermCount=" + termCount );
                    }

                    /*
                    * In order to avoid the browser to cache the response to that request
                    * we stick at its end of the generated URL.
                    */
                    String randomParam = "&now=" + System.currentTimeMillis();
                    String directHighlightUrl = getDirectHighlightUrl( applicationPath, termId, termType, randomParam );
                    String hierarchViewURL = getHierarchViewUrl( randomParam, applicationPath );

                    String quickUrl = this.applicationPath + "/citationDetails.do?externalId=" + termId + "&dataSource=MED&format=contentonly";

                    String quickGraphUrl = this.applicationPath + "//citationDetails.do?externalId="
                                           + termId + "&dataSource=MED&intact=true&format=contentonly&url="
                                           + hierarchViewURL + "&frame=_top";

                    boolean selected = false;
                    if ( selectedTerms != null && selectedTerms.contains( termId ) ) {
                        if ( logger.isDebugEnabled() ) logger.debug( termId + " SELECTED" );
                        selected = true;
                    }

                    urls.add( new SourceBean( termId, termType, termDescription, termCount,
                                              quickUrl, quickGraphUrl, directHighlightUrl, selected, applicationPath ) );
                }

                // sort the source list by count
                Collections.sort( urls );
            }
        }

        return urls;
    }
}
