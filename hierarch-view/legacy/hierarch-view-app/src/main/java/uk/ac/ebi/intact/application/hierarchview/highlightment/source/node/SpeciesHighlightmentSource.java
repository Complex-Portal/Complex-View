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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.highlightment.source.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Organism;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.service.graph.Node;

import java.util.*;

/**
 * Interface allowing to wrap an highlightment source.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class SpeciesHighlightmentSource extends NodeHighlightmentSource {

    private static final Log logger = LogFactory.getLog( SpeciesHighlightmentSource.class );

    /**
     * The key for this source 'Species'
     */
    public static final String SOURCE_KEY;

    /**
     * The class for this source 'specie'
     */
    static final String SOURCE_CLASS;

    private static final String speciePath;

    private static HashMap<String, Organism> specieRefMap;

    private static Map<String, Set<String>> specieNodeMap;

    static {

        // get in the Highlightment properties file where is interpro
        Properties props = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null == props ) {
            String msg = "Unable to find the Species hostname. "
                         + "The properties file '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' couldn't be loaded.";
            logger.error( msg );
            throw new IntactException( msg );
        }
        SOURCE_KEY = props.getProperty( "highlightment.source.node.Species.label" );

        if ( null == SOURCE_KEY ) {
            String msg = "Unable to find the Species Label. "
                         + "Check the 'highlightment.source.node.Species.label' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        speciePath = props.getProperty( "highlightment.source.node.Species.applicationPath" );

        if ( null == speciePath ) {
            String msg = "Unable to find the Species speciePath. "
                         + "Check the 'highlightment.source.node.Species.speciePath' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        SOURCE_CLASS = props.getProperty( "highlightment.source.node.Species.class" );

        if ( null == SOURCE_CLASS ) {
            String msg = "Unable to find the Species Class. "
                         + "Check the 'highlightment.source.node.Species.class' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }
    }

    public static void addToSourceMap( String termId, Organism termObject ) {
        if ( specieRefMap == null ) {
            specieRefMap = new HashMap<String, Organism>();
        }
        specieRefMap.put( termId, termObject );
    }

    public static void addToNodeMap( String termId, Node node ) {
        if ( specieNodeMap == null ) {
            specieNodeMap = new Hashtable<String, Set<String>>();
        }

        // the nodes realted to the given sourceID are fetched
        Set<String> sourceNodes = specieNodeMap.get( termId );

        // if no set exists a new one is created and put into the sourceMap
        if ( sourceNodes == null ) {
            // a hashset is used to avoid duplicate entries
            sourceNodes = new HashSet<String>();
            specieNodeMap.put( termId, sourceNodes );
        }
        sourceNodes.add( node.getId() );
    }

    public Map<String, Set<String>> getNodeMap() {
        return specieNodeMap;
    }

    public List getSourceUrls( Network network,
                               Collection<String> selectedSourceTerms,
                               String applicationPath ) {

        List<SourceBean> urls = new ArrayList();

        // filter to keep only Species terms
        if ( specieNodeMap == null || specieNodeMap.isEmpty() ) {
            network.initHighlightMap();
        }

        if ( specieNodeMap != null && !specieNodeMap.isEmpty() ) {
            Set<String> keySet = specieNodeMap.keySet();

            if ( keySet != null && !keySet.isEmpty() ) {
                Set<String> cloneKeySet = new HashSet();
                cloneKeySet.addAll( keySet );
                keySet = cloneKeySet;

                for ( String termId : keySet ) {

                    String termType = SOURCE_KEY;
                    String termDescription = null;

                    if (specieRefMap != null){
                        Organism organism = specieRefMap.get( termId );
                        if ( organism != null && organism.getIdentifiers() != null) {
                            Collection<CrossReference> refs = organism.getIdentifiers();
                            if ( !refs.isEmpty() && refs.iterator().next() != null) {
                                termDescription = refs.iterator().next().getText();
                            }
                        }
                    }
                    int termCount = specieNodeMap.get( termId ).size();

                    // to summarize
                    if ( logger.isDebugEnabled() )
                        logger.debug( "TermType=" + termType + " | " +
                                      "TermId=" + termId + " | " +
                                      "TermDescription=" + termDescription + " | " +
                                      "TermCount=" + termCount );

                    /*
                    * In order to avoid the browser to cache the response to that request
                    * we stick at its end of the generated URL.
                    */
                    String randomParam = "&now=" + System.currentTimeMillis();


                    String directHighlightUrl = getDirectHighlightUrl( applicationPath, termId, termType, randomParam );
                    String hierarchViewURL = getHierarchViewUrl( randomParam, applicationPath );

                    String quickUrl = null;
                    String quickGraphUrl = null;


                    quickUrl = speciePath + "/?termId=" + termId + "&format=contentonly";

                    quickGraphUrl = speciePath + "/?termId="
                                    + termId + "&intact=true&format=contentonly&url="
                                    + hierarchViewURL + "&frame=_top";


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
                                              quickUrl, quickGraphUrl, directHighlightUrl, selected, applicationPath ) );

                }

                // sort the source list by count
                Collections.sort( urls );
            }
        }
        return urls;
    }
}
