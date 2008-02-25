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

    /**
     * The class for this source 'confidence'
     */
    public static final String SOURCE_CLASS;

    private static ThreadLocal<Map<String, Confidence>> confidenceRefMap = new ThreadLocal<Map<String, Confidence>>() {
        @Override
        protected Map<String, Confidence> initialValue() {
            return new HashMap<String,Confidence>();
        }
    };
    private static ThreadLocal<Map<String, Set<String>>> confidenceEdgeMap = new ThreadLocal<Map<String, Set<String>>>(){
        @Override
        protected Map<String, Set<String>> initialValue() {
            return new HashMap<String,Set<String>>();
        }
    };

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

    public static void addToSourceMap( String termId, Confidence termObject ) {
        if ( confidenceRefMap.get() == null ) {
            confidenceRefMap.set(new HashMap<String, Confidence>());
        }
        confidenceRefMap.get().put( termId, termObject );
    }

    public static void addToEdgeMap( String value, Edge edge ) {
        if ( confidenceEdgeMap.get() == null ) {
            confidenceEdgeMap.set(new Hashtable<String, Set<String>>());
        }

        // the nodes realted to the given sourceID are fetched
        Set<String> sourceEdges = confidenceEdgeMap.get().get( value );

        // if no set exists a new one is created and put into the sourceMap
        if ( sourceEdges == null ) {
            // a hashset is used to avoid duplicate entries
            sourceEdges = new HashSet<String>();
            confidenceEdgeMap.get().put( value, sourceEdges );
        }
        sourceEdges.add( edge.getId() );
    }

    public Map<String, Set<String>> getEdgeMap(){
        return confidenceEdgeMap.get();
    }

    public List<SourceBean> getSourceUrls( Network network,
                                           Collection<String> selectedSourceTerms,
                                           String applicationPath ) {

        List<SourceBean> urls = new ArrayList();
        if ( confidenceEdgeMap == null || confidenceEdgeMap.get().isEmpty() ) {
            network.initHighlightMap();
        }

        if ( confidenceEdgeMap != null && !confidenceEdgeMap.get().isEmpty() ) {

            Set<String> keySet = confidenceEdgeMap.get().keySet();

            if ( keySet != null && !keySet.isEmpty() ) {

                List<double[]> ranges = new ArrayList<double[]>();
                double max = 5;
                for ( double i = 0; i < max; i++ ) {
                    ranges.add( new double[]{i / max, ( i + 1 ) / max} );
                }

                for ( String termInfo : keySet ) {
                    String termType = SOURCE_KEY;
                    String termDescription = termInfo;
                    String termId = null;

                    if ( confidenceRefMap != null ) {
                        Confidence confidence = confidenceRefMap.get().get( termInfo );
                        if ( confidence != null && confidence.getValue() != null ) {
                            double value = Double.valueOf( confidence.getValue() );
                            termId = "";
                            for ( double[] range : ranges ) {
                                if ( value > range[0] && value <= range[1] ) {
                                    termDescription = confidence.getType();//+ ": " + range[0] + "..." + range[1];
                                    break;
                                }
                            }

                            if ( confidence.getType() != null ) {
                                termId = confidence.getValue();
                            }
                        }
                    }

                    int termCount = confidenceEdgeMap.get().get( termId ).size();

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

    public static void clear() {
         confidenceEdgeMap.get().clear();
         confidenceRefMap.get().clear();
    }
}
