package uk.ac.ebi.intact.application.hierarchview.highlightment.source.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.service.graph.Node;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Interface allowing to wrap an highlightment source.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 */

public class InterproHighlightmentSource extends NodeHighlightmentSource {

    private static final Log logger = LogFactory.getLog( InterproHighlightmentSource.class );

    private static final String ATTRIBUTE_OPTION_CHILDREN = "CHILDREN";
    private static final String PROMPT_OPTION_CHILDREN = "With children of the selected Interpro term";

    /**
     * The key for this source
     */
    public static final String SOURCE_KEY;

    /**
     * The class for this source
     */
    static final String SOURCE_CLASS;


    private static final String interproPath;

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

    /**
     * Return the html code for specific options of the source to integrate int
     * the highlighting form. if the method return null, the source hasn't
     * options.
     *
     * @return the html code for specific options of the source.
     */
    public String getHtmlCodeOption( HttpSession aSession ) {
        String htmlCode;
        String userKey = uk.ac.ebi.intact.application.hierarchview.business.Constants.USER_KEY;
        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( userKey );
        String check = ( String ) user.getHighlightOption( ATTRIBUTE_OPTION_CHILDREN );

        if ( check == null ) {
            check = "";
        }

        htmlCode = "<input type=\"checkbox\" name=\""
                   + ATTRIBUTE_OPTION_CHILDREN + "\" " + check
                   + " value=\"checked\">" + PROMPT_OPTION_CHILDREN;

        return htmlCode;
    }

    /**
     * Returns a collection of proteins to be highlighted in the graph.
     *
     * @param network       the graph
     * @param selectedTerms the selected Terms
     * @return
     */
    public Collection proteinToHighlightSourceMap( Network network, Collection<String> selectedTerms ) {

        Collection<Node> nodeList = new ArrayList( 20 ); // should be enough for 90% cases

        // retrieve the set of proteins to highlight for the source key (e.g. GO) and the selected GO Terms
        Set<Node> proteinsToHighlight = null;
        if ( selectedTerms != null ) {
            for ( String selectedTerm : selectedTerms ) {
                proteinsToHighlight = network.getNodesForHighlight( SOURCE_KEY, selectedTerm );
                // if we found any proteins we add all of them to the collection
                if ( proteinsToHighlight != null ) {
                    nodeList.addAll( proteinsToHighlight );
                }
            }
        }

        return nodeList;
    }

    public List<SourceBean> getSourceUrls( Network network,
                                           Collection<String> selectedSourceTerms,
                                           String applicationPath ) {

        List<SourceBean> urls = new ArrayList();

        // filter to keep only Interpro terms
        if ( network.isNodeHighlightMapEmpty() ) {
            network.initHighlightMap();
        }

        Map highlightInterproMap = ( Map ) network.getNodeHighlightMap().get( SOURCE_KEY );

        if ( highlightInterproMap != null && !highlightInterproMap.isEmpty() ) {            
            Set<String> keySet = highlightInterproMap.keySet();

            if ( keySet != null && !keySet.isEmpty() ) {
                Set<String> cloneKeySet = new HashSet();
                cloneKeySet.addAll( keySet );
                keySet = cloneKeySet;
                for ( String termInfo : keySet ) {
                    String termType = SOURCE_KEY;
                    String termId = termInfo;
                    String termDescription = null;

                    CrossReference xref = network.getCrossReferenceById( termInfo );
                    if ( xref != null ) {
                        termId = xref.getIdentifier();
                        if ( xref.hasText() ) {
                            termDescription = xref.getText();
                        }
                    }

                    int interproTermCount = network.getDatabaseTermCount( termType, termInfo );

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
