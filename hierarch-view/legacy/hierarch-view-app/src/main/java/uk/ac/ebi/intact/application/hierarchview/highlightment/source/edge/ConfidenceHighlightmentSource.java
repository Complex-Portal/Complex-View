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
import psidev.psi.mi.tab.model.Confidence;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.service.graph.Edge;

import java.util.*;

/**
 * Interface allowing to wrap an highlightment source.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class ConfidenceHighlightmentSource extends EdgeHighlightmentSource {

    private static final Log logger = LogFactory.getLog( ConfidenceHighlightmentSource.class );

    /**
     * The key for this source 'confidence'
     */
    public static final String SOURCE_KEY;

    public static final String SOURCE_CLASS;

    static {
        // get in the Highlightment properties file where is interpro
        Properties props = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null == props ) {
            String msg = "Unable to find the Confidence hostname. "
                         + "The properties file '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' couldn't be loaded.";
            logger.error( msg );
            throw new IntactException( msg );
        }
        SOURCE_KEY = props.getProperty( "highlightment.source.edge.Confidence.label" );

        if ( null == SOURCE_KEY ) {
            String msg = "Unable to find the Confidence Label. "
                         + "Check the 'highlightment.source.edge.Confidence.label' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        SOURCE_CLASS = props.getProperty( "highlightment.source.edge.Confidence.class" );

        if ( null == SOURCE_CLASS ) {
            String msg = "Unable to find the Confidence Class. "
                         + "Check the 'highlightment.source.edge.Confidence.class' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }
    }

    /**
     * Returns a collection of proteins to be highlighted in the graph.
     * <p/>
     * Method is called when the graph was built by the mine database table.
     *
     * @param network       the network
     * @param selectedTerms the selected Terms
     * @return
     */
    public Collection<Edge> edgeToHighlightSourceMap( Network network, Collection<String> selectedTerms ) {

        Collection<Edge> edgeList = new ArrayList( 20 ); // should be enough for 90% cases

        // retrieve the set of proteins to highlight for the source key (e.g. GO) and the selected GO Terms
        Set<Edge> edgesToHighlight = null;
        if ( selectedTerms != null ) {

            for ( String selectedTerm : selectedTerms ) {
                edgesToHighlight = network.getEdgesForHighlight( SOURCE_KEY, selectedTerm );
                // if we found any proteins we add all of them to the collection
                if ( edgesToHighlight != null ) {
                    edgeList.addAll( edgesToHighlight );
                }
            }
        }

        return edgeList;
    }

    public List<SourceBean> getSourceUrls( Network network,
                                           Collection<String> selectedSourceTerms,
                                           String applicationPath ) {

        List<SourceBean> urls = new ArrayList();
        if ( network.isEdgeHighlightMapEmpty() ) {
            network.initHighlightMap();
        }
        Map highlightConfidenceMap = ( Map ) network.getEdgeHighlightMap().get( SOURCE_KEY );

        if ( highlightConfidenceMap != null && !highlightConfidenceMap.isEmpty() ) {

            Set<String> keySet = highlightConfidenceMap.keySet();

            if ( keySet != null && !keySet.isEmpty() ) {

                List<double[]> ranges = new ArrayList<double[]>();
                double max = 5;
                for ( double i = 0; i < max; i++ ) {
                    ranges.add( new double[]{i / max, ( i + 1 ) / max} );
                }

                for ( String termInfo : keySet ) {
                    String termType = SOURCE_KEY;

                    Confidence confidence = network.getConfidenceByKey( termInfo );
                    String termDescription = termInfo;
                    String termId = null;
                    if ( confidence != null && confidence.getValue() != null ) {
                        double value = Double.valueOf( confidence.getValue() );
                        termId = "";
                        for ( double[] range : ranges ) {
                            if ( value > range[0] && value <= range[1] ) {
                                termDescription = confidence.getType() + ": " + range[0] + "..." + range[1];
                                break;
                            }
                        }

                        if ( confidence.getType() != null ) {
                            termId = confidence.getValue();
                        }
                    }

                    int termCount = network.getDatabaseTermCount( SOURCE_KEY, termInfo );

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
                    String directHighlightUrl = null;

                    boolean selected = false;
                    if ( selectedSourceTerms != null ) {
                        if ( selectedSourceTerms.contains( termId ) ) {
                            if ( logger.isInfoEnabled() ) logger.info( termId + " SELECTED" );
                            selected = true;
                        }
                        directHighlightUrl = getDirectHighlightUrl( applicationPath, termId, selectedSourceTerms, termType, randomParam );
                    } else {
                        directHighlightUrl = getDirectHighlightUrl( applicationPath, termId, termType, randomParam );
                    }

                    urls.add( new SourceBean( termId, termType, termDescription, termCount,
                                              null, null, directHighlightUrl, selected, applicationPath ) );

                }

                // sort the source list by count
                Collections.sort( urls );
            }
        }

        return urls;
    }
}
