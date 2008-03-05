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

import javax.servlet.http.HttpServletRequest;
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

    private Map<String, Confidence> confidenceRefMap;

    private Map<String, Set<String>> confidenceEdgeMap;

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

    public ConfidenceHighlightmentSource() {
        confidenceRefMap = new HashMap<String, Confidence>();
        confidenceEdgeMap = new Hashtable<String, Set<String>>();
    }

    public void addToSourceMap( String termId, Confidence termObject ) {
        confidenceRefMap.put( termId, termObject );
    }

    public void addToEdgeMap( String value, Edge edge ) {
        // the nodes realted to the given sourceID are fetched
        Set<String> sourceEdges = confidenceEdgeMap.get( value );

        // if no set exists a new one is created and put into the sourceMap
        if ( sourceEdges == null ) {
            // a hashset is used to avoid duplicate entries
            sourceEdges = new HashSet<String>();
            confidenceEdgeMap.put( value, sourceEdges );
        }
        sourceEdges.add( edge.getId() );
    }

    public Map<String, Set<String>> getEdgeMap(){
        return confidenceEdgeMap;
    }

    public List<SourceBean> getSourceUrls(Network network,
                                          Collection<String> selectedSourceTerms,
                                          HttpServletRequest request, String applicationPath) {

        List<SourceBean> urls = new ArrayList();
//        if ( confidenceEdgeMap == null || confidenceEdgeMap.isEmpty() ) {
//            network.initHighlightMap(request);
//        }

        if ( confidenceEdgeMap != null && !confidenceEdgeMap.isEmpty() ) {

            Set<String> keySet = confidenceEdgeMap.keySet();

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
                        Confidence confidence = confidenceRefMap.get( termInfo );
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

                    int termCount = confidenceEdgeMap.get( termId ).size();

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

    public void prepare() {
        confidenceEdgeMap.clear();
        confidenceRefMap.clear();
    }

    public static ConfidenceHighlightmentSource getInstance(HttpServletRequest request) {
        String attName = ConfidenceHighlightmentSource.class.getName();

        final ConfidenceHighlightmentSource source;

        if (request.getSession().getAttribute(attName) != null) {
            source = (ConfidenceHighlightmentSource) request.getSession().getAttribute(attName);
        } else {
            source = new ConfidenceHighlightmentSource();
            request.getSession().setAttribute(attName, source);
        }

        return source;
    }

     public Set<String> getHighlightKeys(String clickedId){
        String [] aux = clickedId.split( " - " );
        Double dL = Double.valueOf( aux[0].substring( 1 ));
        Double dU = Double.valueOf( aux[1] );

        Set<String> keys = new HashSet<String>();

        for ( Iterator<String> iter = confidenceEdgeMap.keySet().iterator(); iter.hasNext(); ) {
            String key =  iter.next();

        }

        for ( Iterator<String> iter = confidenceRefMap.keySet().iterator(); iter.hasNext(); ) {
                    String key =  iter.next();

                }


        return keys;
    }
}

