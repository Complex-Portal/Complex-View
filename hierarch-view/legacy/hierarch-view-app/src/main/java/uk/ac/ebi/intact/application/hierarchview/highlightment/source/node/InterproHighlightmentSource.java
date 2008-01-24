package uk.ac.ebi.intact.application.hierarchview.highlightment.source.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.CrossReference;
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
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 */

public class InterproHighlightmentSource extends NodeHighlightmentSource {

    private static final Log logger = LogFactory.getLog( InterproHighlightmentSource.class );

    /**
     * The key for this source
     */
    public static final String SOURCE_KEY;

    /**
     * The class for this source
     */
    static final String SOURCE_CLASS;

    private static final String interproPath;

    private static HashMap<String, CrossReference> interproRefMap;

    private static Map<String, Set<String>> interproNodeMap;

    static {

        // get in the Highlightment properties file where is interpro
        Properties props = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null == props ) {
            String msg = "Unable to find the Interpro hostname. "
                         + "The properties file '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' couldn't be loaded.";
            logger.error( msg );
            throw new IntactException( msg );
        }

        SOURCE_KEY = props.getProperty( "highlightment.source.node.Interpro.label" );

        if ( null == SOURCE_KEY ) {
            String msg = "Unable to find the Interpro label. "
                         + "Check the 'highlightment.source.node.Interpro.label' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        interproPath = props.getProperty( "highlightment.source.node.Interpro.applicationPath" );

        if ( null == interproPath ) {
            String msg = "Unable to find the Interpro hostname. "
                         + "Check the 'highlightment.source.node.Interpro.applicationPath' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        SOURCE_CLASS = props.getProperty( "highlightment.source.node.Interpro.class" );

        if ( null == SOURCE_CLASS ) {
            String msg = "Unable to find the Interpro Class. "
                         + "Check the 'highlightment.source.node.Interpro.class' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }
    }

    public static void addToSourceMap( String termId, CrossReference termObject ) {
        if ( interproRefMap == null ) {
            interproRefMap = new HashMap<String, CrossReference>();
        }
        interproRefMap.put( termId, termObject );
    }

    public static void addToNodeMap( String termId, Node node ) {
        if ( interproNodeMap == null ) {
            interproNodeMap = new Hashtable();
        }

        // the nodes realted to the given sourceID are fetched
        Set<String> sourceNodes = interproNodeMap.get( termId );

        // if no set exists a new one is created and put into the sourceMap
        if ( sourceNodes == null ) {
            // a hashset is used to avoid duplicate entries
            sourceNodes = new HashSet<String>();
            interproNodeMap.put( termId, sourceNodes );
        }
        sourceNodes.add( node.getId() );
    }

    public Map<String, Set<String>> getNodeMap() {
        return interproNodeMap;
    }

    public List<SourceBean> getSourceUrls( Network network,
                                           Collection<String> selectedSourceTerms,
                                           String applicationPath ) {

        if ( interproNodeMap == null || interproNodeMap.isEmpty() ) {
            network.initHighlightMap();
        }

        List<SourceBean> urls = new ArrayList();

        if ( interproNodeMap != null && !interproNodeMap.isEmpty() ) {
            Set<String> keySet = interproNodeMap.keySet();

            if ( keySet != null && !keySet.isEmpty() ) {
                Set<String> cloneKeySet = new HashSet();
                cloneKeySet.addAll( keySet );
                keySet = cloneKeySet;
                for ( String termInfo : keySet ) {
                    String termType = SOURCE_KEY;
                    String termId = termInfo;
                    String termDescription = null;

                    if ( interproRefMap != null ) {
                        CrossReference xref = interproRefMap.get( termInfo );
                        if ( xref != null ) {
                            termId = xref.getIdentifier();
                            if ( xref.hasText() ) {
                                termDescription = xref.getText();
                            }
                        }
                    }

                    int interproTermCount = interproNodeMap.get( termInfo ).size();

                    // to summarize
                    if ( logger.isDebugEnabled() )
                        logger.debug( "interproTermType=" + termType + " | " +
                                      "interproTermId=" + termId + " | " +
                                      "interproTermDescription=" + termDescription + " | " +
                                      "interproTermCount=" + interproTermCount );

                    /*
                    * In order to avoid the browser to cache the response to that request
                    * we stick at its end of the generated URL.
                    */
                    String randomParam = "&now=" + System.currentTimeMillis();
                    String directHighlightUrl = null;
                    String quickInterproUrl = interproPath + "/ISearch?query=" + termId;

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

                    urls.add( new SourceBean( termId, termType, termDescription, interproTermCount,
                                              quickInterproUrl, null, directHighlightUrl, selected,
                                              applicationPath ) );
                }

                // sort the source list by count
                Collections.sort( urls );
            }
        }

        return urls;
    } // getSourceUrls
}
