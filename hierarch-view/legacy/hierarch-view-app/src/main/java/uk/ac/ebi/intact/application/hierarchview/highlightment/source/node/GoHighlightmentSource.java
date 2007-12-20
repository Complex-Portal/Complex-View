package uk.ac.ebi.intact.application.hierarchview.highlightment.source.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
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

public class GoHighlightmentSource extends NodeHighlightmentSource {

    private static final Log logger = LogFactory.getLog( GoHighlightmentSource.class );

    /**
     * separator of keys, use to create and parse key string.
     */

    private static final String ATTRIBUTE_OPTION_CHILDREN = "CHILDREN";
    private static final String PROMPT_OPTION_CHILDREN = "With children of the selected GO term";

    /**
     * The key for this source 'go'
     */
    public static final String SOURCE_KEY;

    /**
     * The class for this source 'go'
     */
    public static final String SOURCE_CLASS;

    private static final String applicationPath;


    static {

        // get in the Highlightment properties file where is go
        Properties props = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null == props ) {
            String msg = "Unable to find the go hostname. The properties file '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' couldn't be loaded.";
            logger.error( msg );
            throw new IntactException( msg );
        }

        SOURCE_KEY = props.getProperty( "highlightment.source.node.GO.label" );
        if ( null == SOURCE_KEY ) {
            String msg = "Unable to find the go Label. "
                         + "Check the 'highlightment.source.node.GO.label' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        applicationPath = props.getProperty( "highlightment.source.node.GO.applicationPath" );

        if ( null == applicationPath ) {
            String msg = "Unable to find the interpro hostname. "
                         + "Check the 'highlightment.source.node.GO.applicationPath' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        SOURCE_CLASS = props.getProperty( "highlightment.source.node.GO.class" );

        if ( null == SOURCE_CLASS ) {
            String msg = "Unable to find the GO Class. "
                         + "Check the 'highlightment.source.node.GO.class' property in the '"
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
     * @param aGraph            the graph
     * @param children          the children of the selected GO Term
     * @param selectedGOTerm    the selected GO Term
     * @param searchForChildren whether it shall be searched for the children
     * @return
     */
    private Collection<Node> proteinToHighlightSourceMap( Network aGraph, Collection<String> children,
                                                          String selectedGOTerm, boolean searchForChildren ) {

        Collection<Node> nodeList = new ArrayList( 20 ); // should be enough for 90% cases

        // retrieve the set of proteins to highlight for the source key (e.g. GO) and the selected GO Term
        Set<Node> proteinsToHighlight = aGraph.getNodesForHighlight( SOURCE_KEY, selectedGOTerm );

        // if we found any proteins we add all of them to the collection
        if ( proteinsToHighlight != null ) {
            nodeList.addAll( proteinsToHighlight );
        }

        /*
         * for every children all proteins related to the current GO Term are
         * fetched from the source highlighting map of the graph and added to
         * the collection
         */
        if ( searchForChildren ) {
            for ( String child : children ) {
                proteinsToHighlight = aGraph.getNodesForHighlight( SOURCE_KEY, child );
                if ( proteinsToHighlight != null ) {
                    nodeList.addAll( proteinsToHighlight );
                }
            }
        }
        return nodeList;
    }


    /**
     * Create a set of protein we must highlight in the graph given in
     * parameter. The protein selection is done according to the source keys
     * stored in the IntactUser. Keys are GO terms, so we select (and highlight)
     * every protein which awned that GO term. If the children option is
     * activated, all proteins which owned a children of the selected GO term
     * are selected.
     *
     * @param aSession the session where to find selected keys.
     * @param network  the graph we want to highlight
     * @return a collection of node to highlight
     */
    public Collection<Node> proteinToHightlight( HttpSession aSession, Network network ) {

        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
        Collection children = user.getKeys();
        String selectedGOTerm = user.getSelectedKey();

        if ( children.remove( selectedGOTerm ) ) {
            if ( logger.isDebugEnabled() ) logger.debug( selectedGOTerm + " removed from children collection" );
        }

        // get source option
        String check = ( String ) user.getHighlightOption( ATTRIBUTE_OPTION_CHILDREN );
        boolean searchForChildren;
        if ( check != null ) {
            searchForChildren = check.equals( "checked" );
        } else {
            searchForChildren = false;
        }
        if ( logger.isDebugEnabled() ) logger.debug( "Children option activated ? " + searchForChildren );

        if ( network.isNodeHighlightMapEmpty() ) {
            network.initHighlightMap();
        }

        return proteinToHighlightSourceMap( network, children, selectedGOTerm, searchForChildren );
    }

    public List<SourceBean> getSourceUrls( Network network, Collection<String> selectedTerm,
                                           String applicationPath, IntactUserI user ) {

        List<SourceBean> urls = new ArrayList();

        if ( network.isNodeHighlightMapEmpty() ) {
            network.initHighlightMap();
        }
        Map highlightGOMap = ( Map ) network.getNodeHighlightMap().get( SOURCE_KEY );

        if ( highlightGOMap != null && !highlightGOMap.isEmpty() ) {
            Set<String> keySet = highlightGOMap.keySet();

            if ( keySet != null && !keySet.isEmpty() ) {
                for ( String termInfo : keySet ) {

                    String termType = SOURCE_KEY;

                    CrossReference xref = network.getCrossReferenceById( termInfo );
                    String termId = termInfo;
                    String termDescription = null;
                    if ( xref != null ) {
                        termId = xref.getIdentifier();

                        if ( xref.hasText() ) {
                            termDescription = xref.getText();
                        }
                    }

                    int termCount = network.getDatabaseTermCount( termType, termInfo );

                    // to summarize
                    if ( logger.isDebugEnabled() ) {
                        logger.debug( "goTermType=" + termType + " | " +
                                      "goTermId=" + termId + " | " +
                                      "goTermDescription=" + termDescription + " | " +
                                      "goTermCount=" + termCount );
                    }

                    /*
                    * In order to avoid the browser to cache the response to that request
                    * we stick at its end of the generated URL.
                    */
                    String randomParam = "&now=" + System.currentTimeMillis();

                    String directHighlightUrl = getDirectHighlightUrl( applicationPath, termId, termType, randomParam );
                    String hierarchViewUrl = getHierarchViewUrl( randomParam, applicationPath );

                    String quickGoUrl = this.applicationPath + "/DisplayGoTerm?id=" + termId + "&format=contentonly";

                    String quickGoGraphUrl = this.applicationPath + "/DisplayGoTerm?selected="
                                             + termId + "&intact=true&format=contentonly&url="
                                             + hierarchViewUrl + "&frame=_top";

                    boolean selected = false;
                    if ( selectedTerm != null && selectedTerm.contains( termId ) ) {
                        if ( logger.isInfoEnabled() ) logger.info( termId + " SELECTED" );
                        selected = true;
                    }

                    urls.add( new SourceBean( termId, termType, termDescription, termCount,
                                              quickGoUrl, quickGoGraphUrl, directHighlightUrl, selected,
                                              applicationPath ) );

                }
                // sort the source list by count
                Collections.sort( urls );
            }
        }
        return urls;
    }
}

