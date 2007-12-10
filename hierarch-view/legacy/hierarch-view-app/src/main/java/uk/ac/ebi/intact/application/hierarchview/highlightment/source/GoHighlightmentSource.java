package uk.ac.ebi.intact.application.hierarchview.highlightment.source;

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
import uk.ac.ebi.intact.util.SearchReplace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Interface allowing to wrap an highlightment source.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 */

public class GoHighlightmentSource extends HighlightmentSource {

    private static final Log logger = LogFactory.getLog( GoHighlightmentSource.class );

    /**
     * separator of keys, use to create and parse key string.
     */
    private static final String KEY_SEPARATOR = ",";
    private static final String ATTRIBUTE_OPTION_CHILDREN = "CHILDREN";
    private static final String PROMPT_OPTION_CHILDREN = "With children of the selected GO term";

    /**
     * The key for this source 'go'
     */
    private static final String SOURCE_KEY = "go";

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

        htmlCode = "<input type=\"checkbox\" name=\"" + ATTRIBUTE_OPTION_CHILDREN + "\" " + check
                   + " value=\"checked\">" + PROMPT_OPTION_CHILDREN;

        return htmlCode;
    }

    /**
     * get a collection of XRef and filter to keep only GO terms
     *
     * @param xRef the XRef collection
     * @return a GO term collection or an empty collection if none exists.
     */
    private Collection<String[]> filterInteractorCrossReference( Collection<CrossReference> xRef ) {
        Collection listGOTerm = new ArrayList( xRef.size() ); // size will be >= to needed capacity

        for ( CrossReference aXRef : xRef ) {
            String[] goTerm = new String[3];

            if ( ( aXRef.getDatabase() ).toLowerCase().equals( SOURCE_KEY ) ) {
                goTerm[0] = "Go";
                goTerm[1] = aXRef.getIdentifier();
                goTerm[2] = aXRef.getText();
                listGOTerm.add( goTerm );
                if ( logger.isDebugEnabled() ) logger.debug( aXRef.getIdentifier() );
            }
        }

        return listGOTerm;
    }

    /**
     * Returns a collection of nodes to highlight for the display
     *
     * @param aGraph            the network to display
     * @param selectedGOTerm    the selected GO Term
     * @param children          the children GO Terms of the selected GO Term
     * @param searchForChildren whether it shall be searched for the children
     * @return
     */
    private Collection<Node> proteinToHighlightInteractor( Network aGraph, String selectedGOTerm,
                                                           Collection<String> children,
                                                           boolean searchForChildren ) {

        // if the source highlight map of the network is empty
        // it is filled with the source informations from each
        // node of the graph
        if ( aGraph.isSourceHighlightMapEmpty() ) {
            aGraph.initSourceHighlightMap();
            //user.getHVNetworkBuilder().initSourceHighlightMap( aGraph );
        }
        // return the set of proteins to highlight based on the source
        // highlighting map of the graph
        return proteinToHighlightSourceMap( aGraph, children, selectedGOTerm, searchForChildren );
    }


    /**
     * Returns a collection of proteins to be highlighted in the graph.
     * <p/>
     * Method is called when the graph was built by the mine database table.
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

        // retrieve the set of proteins to highlight for the source key (e.g. GO) and
        // the selected GO Term
        Set<Node> proteinsToHighlight = aGraph.getProteinsForHighlight( SOURCE_KEY, selectedGOTerm );

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
                proteinsToHighlight = aGraph.getProteinsForHighlight( SOURCE_KEY, child );
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
     * @param aGraph   the graph we want to highlight
     * @return a collection of node to highlight
     */
    public Collection<Node> proteinToHightlight( HttpSession aSession, Network aGraph ) {

        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
        Collection children = user.getKeys();
        String selectedGOTerm = user.getSelectedKey();

        logger.debug( "getKeys=" + children + " | selectedGOTerm=" + selectedGOTerm );
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


        return proteinToHighlightInteractor( aGraph, selectedGOTerm, children, searchForChildren );
    }


    /**
     * Allows to update the session object with parameters' request. These
     * parameters are specific of the implementation.
     *
     * @param aRequest request in which we have to get parameters to save in the
     *                 session
     * @param aSession session in which we have to save the parameter
     */
    public void saveOptions( HttpServletRequest aRequest, HttpSession aSession ) {

        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession()
                .getAttribute( Constants.USER_KEY );
        String[] result = aRequest.getParameterValues( ATTRIBUTE_OPTION_CHILDREN );

        if ( result != null )
            user.addHighlightOption( ATTRIBUTE_OPTION_CHILDREN, result[0] );
    } // saveOptions


    public List getSourceUrls( Collection<CrossReference> xRefs,
                               Collection<String> selectedXRefs,
                               String applicationPath,
                               IntactUserI user ) {

        // get in the Highlightment properties file where is go
        Properties props = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null == props ) {
            String msg = "Unable to find the interpro hostname. The properties file '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' couldn't be loaded.";
            logger.error( msg );
            throw new IntactException( msg );
        }

        String goPath = props.getProperty( "highlightment.source.GO.applicationPath" );

        if ( null == goPath ) {
            String msg = "Unable to find the interpro hostname. "
                         + "Check the 'highlightment.source.GO.applicationPath' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        // filter to keep only GO terms
        if ( logger.isDebugEnabled() ) logger.debug( xRefs.size() + " Xref before filtering" );
        Collection<String[]> listGOTerms;
        listGOTerms = filterInteractorCrossReference( xRefs );

        if ( logger.isDebugEnabled() ) logger.debug( listGOTerms.size() + " GO term(s) after filtering" );

        // create url collection with exact size
        List urls = new ArrayList( listGOTerms.size() );

        // list of Nodes of the Graph
        Network aGraph = user.getInteractionNetwork();

        // Create a collection of label-value object (GOterm, URL to access a
        // nice display in go)
        String[] goTermInfo;
        String goTermId, goTermType, goTermDescription;

        /*
         * In order to avoid the browser to cache the response to that request
         * we stick at its end of the generated URL.
         */
        String randomParam = "&now=" + System.currentTimeMillis();

        if ( listGOTerms != null && !listGOTerms.isEmpty() ) {
            for ( String[] listGOTerm : listGOTerms ) {
                goTermInfo = listGOTerm;
                goTermId = goTermInfo[1];
                goTermType = goTermInfo[0];
                goTermDescription = goTermInfo[2];

                int goTermCount = aGraph.getDatabaseTermCount( goTermId );

                if ( logger.isDebugEnabled() ) logger.debug( "goTermCount: " + goTermCount );

                // to summarize
                if ( logger.isDebugEnabled() ) logger.debug( "goTermType=" + goTermType + " | goTermId=" + goTermId
                                                             + " | goTermDescription=" + goTermDescription + " | goTermCount="
                                                             + goTermCount );

                String directHighlightUrl = applicationPath
                                            + "/source.do?keys=${selected-children}&clicked=${id}&type=${type}"
                                            + randomParam;
                String hierarchViewURL = null;

                try {
                    hierarchViewURL = URLEncoder.encode( directHighlightUrl, "UTF-8" );
                }
                catch ( UnsupportedEncodingException e ) {
                    logger.error( e );
                }

                // replace ${selected-children}, ${id} by the GO id and ${type} by Go
                if ( logger.isDebugEnabled() ) logger.debug( "direct highlight URL: " + directHighlightUrl );

                directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${selected-children}", goTermId );
                directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${id}", goTermId );
                directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${type}", goTermType );

                if ( logger.isDebugEnabled() ) logger.debug( "direct highlight URL (modified): " + directHighlightUrl );

                String quickGoUrl = goPath + "/DisplayGoTerm?id=" + goTermId + "&format=contentonly";

                String quickGoGraphUrl = goPath + "/DisplayGoTerm?selected="
                                         + goTermId + "&intact=true&format=contentonly&url="
                                         + hierarchViewURL + "&frame=_top";
                if ( logger.isDebugEnabled() ) logger.debug( "Xref: " + goTermId );

                boolean selected = false;
                if ( selectedXRefs != null && selectedXRefs.contains( goTermId ) ) {
                    if ( logger.isDebugEnabled() ) logger.debug( goTermId + " SELECTED" );
                    selected = true;
                }

                if ( logger.isDebugEnabled() )
                    logger.debug( "Count of GO(" + goTermDescription + ") is " + goTermCount );

                urls.add( new SourceBean( goTermId, goTermType, goTermDescription, goTermCount,
                                          quickGoUrl, quickGoGraphUrl, directHighlightUrl, selected,
                                          applicationPath ) );
            }
        }

        // sort the source list by count
        Collections.sort( urls );

        return urls;
    }


    /**
     * Parse the set of key generate by the source and give back a collection of
     * keys.
     *
     * @param someKeys a string which contains some key separates by a
     *                 character.
     * @return the splitted version of the key string as a collection of String.
     */
    public Collection<String> parseKeys( String someKeys ) {
        Collection keys = new Vector();

        if ( ( null == someKeys ) || ( someKeys.length() < 1 ) ) {
            return null;
        }

        StringTokenizer st = new StringTokenizer( someKeys, KEY_SEPARATOR );

        while ( st.hasMoreTokens() ) {
            String key = st.nextToken();
            keys.add( key );
        }

        return keys;
    }
}

